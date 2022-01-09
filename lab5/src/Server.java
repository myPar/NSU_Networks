import data.ChannelState;
import data.ChannelType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static data.ChannelState.*;
import static data.ChannelType.CLIENT;
import static data.ChannelType.REMOTE;

public class Server {
    private boolean stopped = true;
    private static final Logger LOGGER = Logger.getLogger("Server logger");
    private int curClientsCount = 0;

    static class ResponseData {
        byte port1;
        byte port2;
        byte[] ipAddress;
        byte[] domainName;
        byte code;

        ResponseData(byte port1, byte port2, byte[] ipAddress, byte[] domainName, byte code) {
            this.port1 = port1;
            this.port2 = port2;
            this.ipAddress = ipAddress;
            this.domainName = domainName;
            this.code = code;
        }
    }
    static class ChannelWrap {
        // buffer to read data from channel
        ByteBuffer inBuffer;
        // buffer to write data in, which will be write to the channel then
        ByteBuffer outBuffer;
        // key for remote connection channel
        SelectionKey remoteKey = null;

        ChannelState state;
        final ChannelType type;
        ResponseData responseData = null;

        ChannelWrap(ChannelType type, ChannelState state) {
            this.state = state;
            this.type = type;

            if (this.type == REMOTE) {
                // not need for remote channel at the start
                outBuffer = null;
                inBuffer = null;
            }
            else {
                outBuffer = ByteBuffer.allocate(Constants.MAX_BUFFER_SIZE);
                inBuffer = ByteBuffer.allocate(Constants.MAX_BUFFER_SIZE);
            }
            // remote channel can have NONE state only
            assert this.type != REMOTE || this.state == ChannelState.NONE;
        }
        // set new state
        void setNewState(ChannelState state) {this.state = state;}
        // set remote point selection key
        void setRemoteKey(SelectionKey remoteKey) {
            this.remoteKey = remoteKey;
        }
    }

    // main method of start server process execution
    public synchronized void start(int port) {
        stopped = false;
        // start server process
        new Thread(new ServerProcess(port)).start();
    }
    // stopping server process
    public synchronized void stop() {
        stopped = true;
    }
    // server process starts in separate thread
    private class ServerProcess implements Runnable {
        private final int port;

        private ServerProcess(int port) {
            this.port = port;
        }
        @Override
        public void run() {
            LOGGER.log(Level.INFO, "Server started...");

            try {
                handleClients(port);
            }
            catch (IOException e) {
                LOGGER.log(Level.WARNING, "Server crushed with I/O exception");
            }
        }
    }
    // correct!!!
    private void accept(SelectionKey key) throws IOException {
        // get server socket channel and it's selector
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        Selector selector = key.selector();
        // accept and configure channel
        SocketChannel clientChanel = serverChannel.accept();
        clientChanel.configureBlocking(false);
        // register channel in selector on READING
        SelectionKey newClientKey = clientChanel.register(selector, SelectionKey.OP_READ);

        // attach channel wrapper
        newClientKey.attach(new ChannelWrap(CLIENT, INIT_REQUEST));
    }
    // correct!!!
    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment ();

        SelectionKey clientChannelKey = chWrap.remoteKey;
        ChannelWrap clientChannelWrap = (ChannelWrap) clientChannelKey.attachment();

        // finish the connection to remote channel
        channel.finishConnect();

        // write success response to output buffer of the client key
        SOCKSv5Impl.responseConnectSuccess(clientChannelKey);
        // bind buffers: client in - remote out, client out - remote in
        chWrap.inBuffer = clientChannelWrap.outBuffer;
        chWrap.outBuffer = clientChannelWrap.inBuffer;

        // set client channel state - CONNECTION_RESPONSE_SUCCESS
        clientChannelWrap.setNewState(CONNECTION_RESPONSE_SUCCESS);
        // set client ops: op_write
        clientChannelKey.interestOps(SelectionKey.OP_WRITE);
        // set remote channel ops: no ops, because we should wait till response will be send to client channel
        key.interestOps(0);
    }

    private void read(SelectionKey key) throws IOException {
        ChannelWrap chWrap = (ChannelWrap) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        int count;
        if ((count = channel.read(chWrap.inBuffer)) < 0) {
            // end of the stream case
            closeChannel(key);
        }
        else if (count != 0) {
            if (chWrap.type == CLIENT) {
                switch (chWrap.state) {
                    case INIT_REQUEST: {
                        try {
                            // read init request
                            SOCKSv5Impl.readInitRequest(key);
                            // write response to output buffer
                            SOCKSv5Impl.responseInitSuccess(key);
                            // set op = write for client channel
                            key.interestOps(SelectionKey.OP_WRITE);
                            // set state = INIT_RESPONSE
                            ((ChannelWrap) key.attachment()).setNewState(INIT_RESPONSE_SUCCESS);
                        } catch (SOCKSv5Impl.SOCKSv5Exception e) {
                            SOCKSv5Impl.responseInitFailed(key);
                            // set op = write for client channel
                            key.interestOps(SelectionKey.OP_WRITE);
                            // set state = INIT_RESPONSE
                            ((ChannelWrap) key.attachment()).setNewState(INIT_RESPONSE_FAILED);
                        }
                        return;
                    }
                    case CONNECTION_REQUEST: {
                        try {
                            // read connection request
                            SOCKSv5Impl.readConnectionRequest(key);
                            // wait till connection become established
                            key.interestOps(0);
                        } catch (SOCKSv5Impl.SOCKSv5Exception e) {
                            String description = e.description;
                            byte code = e.code;
                            // set failed connection response write mode and exception code
                            chWrap.responseData = new ResponseData((byte) 0,(byte) 0, null, null, code);
                            SOCKSv5Impl.responseConnectFailed(key);
                            // set state = CONNECTION_RESPONSE_FAILED
                            ((ChannelWrap) key.attachment()).setNewState(CONNECTION_RESPONSE_FAILED);
                            // set write op to send failed message
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                        return;
                    }
                    case DATA_TRANSFER: {
                        break;
                    }
                    default: {
                        // invalid case
                        assert false;
                    }
                }
            }
            else {
                assert chWrap.type == REMOTE;
            }
            // data transfer case
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);                             // remove read
            chWrap.remoteKey.interestOps(chWrap.remoteKey.interestOps() | SelectionKey.OP_WRITE);    // add write to another channel
            // prepare buffer for writing
            chWrap.inBuffer.flip();
        }
    }

    private void closeChannel(SelectionKey key) {
        try {
            // deregister key
            key.cancel();
            // close associated channel
            key.channel().close();
            SelectionKey remoteKey = ((ChannelWrap) key.attachment()).remoteKey;

            if (remoteKey != null) {
                // we should write remaining data to remote channel and also close it later
                ChannelWrap remoteKeyWrap = (ChannelWrap) remoteKey.attachment();
                remoteKeyWrap.remoteKey = null;

                if ((remoteKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                    // remote channel doesn't set in writing mode so buffer was not already flip
                    remoteKeyWrap.outBuffer.flip();
                }
                // set write op to send remaining data to channel
                remoteKey.interestOps(SelectionKey.OP_WRITE);
            }
        }
        catch (IOException e) {
            // fatal: can't close the channel
        }
    }
    private void write(SelectionKey key) throws IOException {
        ChannelWrap chWrap = (ChannelWrap) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        int count;
        count = channel.write(chWrap.outBuffer);

        if (chWrap.type == CLIENT) {
            if (chWrap.outBuffer.remaining() == 0) {
                // all data wrote so clear out buffer
                chWrap.outBuffer.clear();

                switch (chWrap.state) {
                    case INIT_RESPONSE_SUCCESS: {
                        key.interestOps(SelectionKey.OP_READ);
                        chWrap.setNewState(CONNECTION_REQUEST);
                        return;
                    }
                    case INIT_RESPONSE_FAILED:
                    case CONNECTION_RESPONSE_FAILED: {
                        closeChannel(key);
                        return;
                    }
                    case CONNECTION_RESPONSE_SUCCESS: {
                        // connection established set read mode for client and remote channel
                        key.interestOps(SelectionKey.OP_READ);
                        chWrap.remoteKey.interestOps(SelectionKey.OP_READ);
                        // set data traverse state
                        chWrap.setNewState(DATA_TRANSFER);
                        return;
                    }
                    case DATA_TRANSFER: {
                        break;
                    }
                    default:
                        assert false;
                }
            }
        }
        else {
            assert chWrap.type == REMOTE;
        }
        if (chWrap.outBuffer.remaining() == 0) {
            if (chWrap.remoteKey == null) {
                // client channel is close, so close connection for the remote channel
                closeChannel(key);
                return;
            }
            // data transfer case
            key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);                             // remove write
            chWrap.remoteKey.interestOps(chWrap.remoteKey.interestOps() | SelectionKey.OP_READ);    // add read to another channel
        }
    }
    private void handleClients(int port) throws IOException {
        InetAddress localHost = InetAddress.getLocalHost();

        // create selector
        Selector selector = Selector.open();

        // create ServerSocket channel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // non blocking configuration
        serverChannel.configureBlocking(false);
        // bind it to the local host with specified port
        serverChannel.bind(new InetSocketAddress(localHost, port));

        // register server socket channel in Selector on accept action
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // main server routine block
        while (selector.select() > -1) {
            // get registered keys
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            SelectionKey key;

            while(keyIterator.hasNext()) {
                key = keyIterator.next();
                // remove key from set to prevent handling it twice (selector doesn't remove key by itself)
                keyIterator.remove();

                if (key.isValid()) {
                    // accept new client
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isConnectable()) {
                        connect(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                }
            }
        }
    }

}
