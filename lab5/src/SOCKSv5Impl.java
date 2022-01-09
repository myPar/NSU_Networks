import data.ChannelState;
import data.ChannelType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static data.ChannelState.*;
import static data.ChannelType.CLIENT;
import static data.ChannelType.REMOTE;

class SOCKSv5Impl {
    static void responseInitSuccess(SelectionKey key) throws IOException {
        //SocketChannel channel = ((SocketChannel) key.channel());
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment ();

        chWrap.outBuffer.clear();
        chWrap.outBuffer.put(successInitRespond);
        chWrap.outBuffer.flip();

        //channel.write(chWrap.outBuffer);
        //chWrap.setNewState(CONNECTION_REQUEST);
        //key.interestOps(SelectionKey.OP_READ);
    }

    static void responseInitFailed(SelectionKey key) throws IOException {
        //SocketChannel channel = ((SocketChannel) key.channel());
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment ();

        chWrap.outBuffer.clear();
        chWrap.outBuffer.put(failedInitRespond);
        chWrap.outBuffer.flip();
        //channel.write(chWrap.outBuffer);
        // close channel later
    }

    static void responseConnectSuccess(SelectionKey key) throws IOException {
        Server.ResponseData respData = ((Server.ChannelWrap) key.attachment()).responseData;
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        byte[] message;

        if (respData.ipAddress != null) {
            message = new byte[Constants.CONNECT_MESSAGE_IPv4];
            message[3] = Constants.IP;
            // init address:
            message[4] = respData.ipAddress[0];
            message[5] = respData.ipAddress[1];
            message[6] = respData.ipAddress[2];
            message[7] = respData.ipAddress[3];
        }
        else {
            assert respData.domainName != null;
            message = new byte[respData.domainName.length + 1 + 6];
            message[3] = Constants.DN;
            // init domain name:
            System.arraycopy(respData.domainName, 0, message, 4, respData.domainName.length);
        }
        // init header:
        message[0] = Constants.SOCKS_VERSION;
        message[1] = Constants.REQUEST_PROVIDED;
        message[2] = Constants.RESERVED;
        int length = message.length;
        // init port:
        message[length - 2] = respData.port1;
        message[length - 1] = respData.port2;

        // write to channel:
        chWrap.outBuffer.clear();
        chWrap.outBuffer.put(message);
        chWrap.outBuffer.flip();

        //channel.write(chWrap.outBuffer);
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

    static void setWrite(SelectionKey key, Server.ChannelWrap chWrap, ChannelState state) {
        // change channel state (now he is waiting a response)
        chWrap.setNewState(state);
        // remove read op and add write op to client channel
        key.interestOps(SelectionKey.OP_WRITE);
        // clear buffer
        chWrap.inBuffer.clear();
    }

    static void readInitRequest(SelectionKey key) throws SOCKSv5Exception{
        Server.ChannelWrap chWrap = (Server.ChannelWrap) key.attachment();
        assert chWrap.type == CLIENT;
        byte[] data = chWrap.inBuffer.array();

        // check the hello message format
        if (chWrap.inBuffer.position() < Constants.HELLO_MESSAGE_SIZE || data[0] != Constants.SOCKS_VERSION || data[1] != (byte) 0x01 || data[2] != (byte) 0x00) {
            throw new SOCKSv5Exception("invalid hello message format", Constants.INIT_REQUEST_ERROR);
        }
        //setWrite(key, chWrap, INIT_RESPONSE_SUCCESS);
    }

    static void readConnectionRequest(SelectionKey key) throws SOCKSv5Exception {
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
        InetAddress address;
        String name;
        byte port1;
        byte port2;
        int port;
        byte[] addressData = null;
        byte[] nameData = null;

        // IPv4 address case:
        if (data[3] == 0x01) {
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
            port1 = data[8];
            port2 = data[9];

        }
        // domain name case
        else if (data[3] == 0x03) {
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
                builder.append(curByte);
                nameData[i] = curByte;
            }
            name = builder.toString();

            // get address
            try {
                address = InetAddress.getByName(name);
            } catch (UnknownHostException e) {
                throw new SOCKSv5Exception("remote connect failed", Constants.HOST_UNAVAILABLE);
            }
            port1 = data[5 + nameLength];
            port2 = data[5 + nameLength + 1];
        }
        else {
            throw new SOCKSv5Exception("invalid connect message format: dst address type", Constants.ADDRESS_TYPE_UNSUPPORTED);
        }
        // get port:
        port = ((0xff & port1) << 8) | ((0xff & port2));

        // create new remote channel:
        SocketChannel remoteChannel;
        try {
            remoteChannel = SocketChannel.open();
            remoteChannel.configureBlocking(false);
            remoteChannel.connect(new InetSocketAddress(address, port));
        } catch (IOException e) {
            throw new SOCKSv5Exception("remote connect failed", Constants.SOCKS_ERROR);
        }
        // register remote channel on connection:
        SelectionKey remoteChannelKey;
        try {
            remoteChannelKey = remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e) {
            throw new SOCKSv5Exception("channel registering failed", Constants.SOCKS_ERROR);
        }
        // create attachment
        Server.ChannelWrap remoteChannelWrap = new Server.ChannelWrap(REMOTE, NONE);

        // set remote keys for both channels:
        remoteChannelWrap.setRemoteKey(key);
        remoteChannelKey.attach(remoteChannelWrap);

        Server.ChannelWrap channelWrap = (Server.ChannelWrap) key.attachment();
        assert channelWrap.remoteKey == null;
        channelWrap.setRemoteKey(remoteChannelKey);

        // set response data:
        ((Server.ChannelWrap) key.attachment()).responseData = new Server.ResponseData(port1, port2, addressData, nameData, Constants.REQUEST_PROVIDED);
    }
}
