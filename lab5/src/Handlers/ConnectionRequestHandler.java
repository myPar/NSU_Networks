package Handlers;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import Attachments.ConnectionRequestData;
import DNS.AddressGetter;
import DNS.DomainNameResolver;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;
import SOCKS.ConnectionMessage;
import SOCKS.SOCKSv5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

// handle connection request and implements connection to the remote channel
public class ConnectionRequestHandler implements Handler, Connector {
    @Override
    public void handle(SelectionKey key) throws Exception {
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;
        SocketChannel clientChannel = (SocketChannel) channel;

        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        ByteBuffer buffer = attachment.getIn();

        if (clientChannel.read(buffer) == -1) {
            // EOS reached
            buffer.flip();
            byte[] requestData = Arrays.copyOfRange(buffer.array(), 0, buffer.limit());
            buffer.clear();

            ConnectionMessage message = SOCKSv5.parseConnectRequest(requestData);

            // get port value and it's bytes:
            int port = message.portNumber;
            byte[] portBytes = message.portBytes;
            assert portBytes.length == 2;

            // get address value and it's bytes:
            String address = message.addressValue;
            byte[] addressBytes = message.addressBytes;

            // add connection request data to attachment; will use in response sending:
            ConnectionRequestData additionalData = new ConnectionRequestData(portBytes[0], portBytes[1], addressBytes, message.AddressType);
            attachment.setConnectionRequestData(additionalData);

            if (message.AddressType == SOCKSv5.DN) {
                // request on resolving domain name
                DomainNameResolver.getResolver().resolveDomainName(address, new DomainNameResolver.KeyData(port, key));
                key.interestOps(0);
            }
            else {
                InetSocketAddress dstAddress;
                try {
                    dstAddress = AddressGetter.getAddress(address, port);
                }
                catch (UnknownHostException e) {
                    throw new HandlerException(Classes.CONNECTION_RQST, "can't parse address - " + address, Types.UH);
                }
                attachment.setRemoteAddress(dstAddress);
                connectToChannel(key);
            }
        }
    }
    @Override
    public void connectToChannel(SelectionKey key) throws Exception {
        assert key.attachment() instanceof CompleteAttachment;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        InetSocketAddress address = attachment.getRemoteAddress();
        assert address != null;

        // establish remote channel connection
        SocketChannel remoteChannel;
        try {
            remoteChannel = SocketChannel.open();
            remoteChannel.configureBlocking(false);
            remoteChannel.connect(address);
        } catch (IOException e) {
            throw new HandlerException(Classes.CONNECTION_RQST, "can't init connection to remote host", Types.IO);
        }
        // refactor attachments and channel's operations:
        attachment.setRemoteChannel(remoteChannel);
        key.interestOps(0);

        // create and init remote channel attachment:
        CompleteAttachment remoteChannelAttachment = new CompleteAttachment(KeyState.FINISH_REMOTE_CONNECT, false);
        remoteChannelAttachment.setIn(attachment.getOut());
        remoteChannelAttachment.setOut(attachment.getIn());
        remoteChannelAttachment.setRemoteChannel(key.channel());

        // register remote channel on connect to finish the connection
        remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT);
    }
}
