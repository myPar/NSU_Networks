import LogData.LogData.Status;
import LogData.LogData.Type;
import LogData.LogData;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import static data.ChannelState.*;
import static data.ChannelType.CLIENT;
import static data.ChannelType.REMOTE;

class SOCKSv5Impl {
    static void responseInitSuccess(SelectionKey key) {
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment ();

        chWrap.outBuffer.clear();
        chWrap.outBuffer.put(successInitRespond);
        chWrap.outBuffer.flip();
    }

    static void responseInitFailed(SelectionKey key) {
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment ();

        chWrap.outBuffer.clear();
        chWrap.outBuffer.put(failedInitRespond);
        chWrap.outBuffer.flip();
    }

    static void responseConnectSuccess(SelectionKey key) {
        Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.CONNECT, Status.IN_PROCESS, "write response"));

        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment();
        Server.ResponseData respData = chWrap.responseData;

        chWrap.outBuffer.clear();
        // write header:
        chWrap.outBuffer.put(Constants.SOCKS_VERSION)
                .put(Constants.REQUEST_PROVIDED)
                .put(Constants.RESERVED);
        if (respData.ipAddress != null) {
            chWrap.outBuffer.put(Constants.IP)
                            .put(respData.ipAddress);
        }
        else {
            chWrap.outBuffer.put((byte) respData.domainName.length)
                            .put(respData.domainName);
        }
        chWrap.outBuffer.putShort(respData.port);
        chWrap.outBuffer.flip();

        Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.CONNECT, Status.SUCCESS, "connection response wrote"));
    }

    static void responseConnectFailed(SelectionKey key) throws IOException {
        // send header + exception code + garbage data
        Server.ResponseData respData = ((Server.ChannelWrap) key.attachment()).responseData;
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment();

        byte[] message = {Constants.SOCKS_VERSION, respData.code, Constants.RESERVED, Constants.IP, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        // write to buffer:
        chWrap.outBuffer.clear();
        chWrap.outBuffer.put(message);
        chWrap.outBuffer.flip();
    }

    static class SOCKSv5Exception extends Exception {
        String description;
        byte code;

        SOCKSv5Exception(String str, byte code) {
            description = str;
            this.code = code;
        }
    }
    // SOCKS version - 5, count of auth methods - 1, number of auth method - 0
    private static final byte[] successInitRespond = {Constants.SOCKS_VERSION, (byte) 0x00};
    private static final byte[] failedInitRespond = {Constants.SOCKS_VERSION, (byte) 0xff};

    static void readInitRequest(SelectionKey key) throws SOCKSv5Exception{
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment();
        assert chWrap.type == CLIENT;
        byte[] data = chWrap.inBuffer.array();

        // check the hello message format
        if (chWrap.inBuffer.position() < Constants.HELLO_MESSAGE_SIZE || data[0] != Constants.SOCKS_VERSION || data[1] != (byte) 0x01 || data[2] != (byte) 0x00) {
            throw new SOCKSv5Exception("invalid hello message format", Constants.INIT_REQUEST_ERROR);
        }
        // request checked, clear the buffer
        chWrap.inBuffer.clear();
    }

    static void readConnectionRequest(SelectionKey key) throws SOCKSv5Exception, IOException {
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment();
        assert chWrap.type == CLIENT;
        byte[] data = chWrap.inBuffer.array();

        // check connection request message length
        if (chWrap.inBuffer.position() < Constants.CONNECT_MESSAGE_SIZE) {
            throw new SOCKSv5Exception("invalid connect message format: length", Constants.PROTOCOL_ERROR);
        }
        // check format
        if (data[0] != Constants.SOCKS_VERSION || data[1] != (byte) 0x01 || data[2] != (byte) 0x00) {
            throw new SOCKSv5Exception("invalid connect message format: header", Constants.PROTOCOL_ERROR);
        }
        Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.SUCCESS, "connection request format checked"));
        InetAddress address;
        String name;
        byte port1;
        byte port2;
        int port;
        byte[] addressData = null;
        byte[] nameData = null;

        // IPv4 address case:
        if (data[3] == 0x01) {
            Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.IN_PROCESS, "ipv4 address case"));
            if (chWrap.inBuffer.position() < Constants.CONNECT_MESSAGE_IPv4) {
                // invalid length
                throw new SOCKSv5Exception("invalid connect message (dst address type - ipv4) format: length", Constants.PROTOCOL_ERROR);
            }
            // get ip address:
            addressData = new byte[] {data[4], data[5], data[6], data[7]};

            try {
                address = InetAddress.getByAddress(addressData);
            }
            catch (UnknownHostException e) {
                throw new SOCKSv5Exception("invalid connect message format: ipv4 address", Constants.HOST_UNAVAILABLE);
            }
            Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.SUCCESS, "ipv4 address got: " + address.toString()));
            port1 = data[8];
            port2 = data[9];
        }
        // domain name case
        else if (data[3] == 0x03) {
            Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.IN_PROCESS, "domain name case"));
            if (chWrap.inBuffer.position() < Constants.CONNECT_MESSAGE_DN) {
                throw new SOCKSv5Exception("invalid connect message (dst address type - domain name) format: length", Constants.PROTOCOL_ERROR);
            }
            // get domain name length
            int nameLength = data[4];
            if (chWrap.inBuffer.position() < Constants.CONNECT_MESSAGE_DN - 1 + nameLength) {
                throw new SOCKSv5Exception("invalid connect message (dst address type - domain name) format: length", Constants.PROTOCOL_ERROR);
            }
            // build domain name:
            nameData = new byte[nameLength];
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < nameLength; i++) {
                byte curByte = data[5 + i];
                builder.append((char) curByte);
                nameData[i] = curByte;
            }
            name = builder.toString();

            Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.SUCCESS, "domain name got: " + name));
            // get address
            try {
                address = InetAddress.getByName(name);
            } catch (UnknownHostException e) {
                throw new SOCKSv5Exception("remote connect failed", Constants.HOST_UNAVAILABLE);
            }
            Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.SUCCESS, "address got by dn: " + address.toString()));
            port1 = data[5 + nameLength];
            port2 = data[5 + nameLength + 1];
        }
        else {
            throw new SOCKSv5Exception("invalid connect message format: dst address type unsupported", Constants.ADDRESS_TYPE_UNSUPPORTED);
        }
        // get port:
        port = ((0xff & port1) << 8) | ((0xff & port2));
        Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.IN_PROCESS, "remote channel port: " + port));
        Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.IN_PROCESS, "init connection to remote channel..."));
        // create new remote channel:
        SocketChannel remoteChannel;
        try {
            remoteChannel = SocketChannel.open();
            remoteChannel.configureBlocking(false);
            remoteChannel.connect(new InetSocketAddress(address, port));
        } catch (IOException e) {
            throw new SOCKSv5Exception("remote connect failed", Constants.SOCKS_ERROR);
        }
        Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.SUCCESS, "connection init completed"));

        // register remote channel on connection:
        SelectionKey remoteChannelKey;
        try {
            remoteChannelKey = remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e) {
            throw new SOCKSv5Exception("channel registering failed", Constants.SOCKS_ERROR);
        }
        Server.LOGGER.log(Level.INFO, LogData.getMessage(Type.READ, Status.SUCCESS, "remote channel registered in selector"));

        // create attachment
        Server.ChannelWrap remoteChannelWrap = new Server.ChannelWrap(REMOTE, NONE);

        // set remote keys for both channels:
        remoteChannelWrap.setRemoteKey(key);
        remoteChannelKey.attach(remoteChannelWrap);

        Server.ChannelWrap channelWrap = (Server.ChannelWrap) key.attachment();
        assert channelWrap.remoteKey == null;
        channelWrap.setRemoteKey(remoteChannelKey);

        // clear in buffer
        chWrap.inBuffer.clear();
    }
}
