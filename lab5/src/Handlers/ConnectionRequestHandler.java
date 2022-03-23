package Handlers;

import Attachments.CompleteAttachment;
import Attachments.BaseAttachment.KeyState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

// handle connection request and implements connection to the remote channel
public class ConnectionRequestHandler implements Handler, Connector {
    @Override
    public void handle(SelectionKey key) throws Exception {

    }
    @Override
    public void connectToChannel(SelectionKey key) throws Exception {
        assert key.attachment() instanceof CompleteAttachment;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        InetSocketAddress address = attachment.getRemoteAddress();
        assert address != null;

        // establish remote channel connection
        SocketChannel remoteChannel = null;
        try {
            remoteChannel = SocketChannel.open();
            remoteChannel.configureBlocking(false);
            remoteChannel.connect(address);
        } catch (IOException e) {
            e.printStackTrace();    // TODO implement exception handling
            assert false;
        }
        // refactor attachments and channel's operations:
        attachment.setRemoteChannel(remoteChannel);
        key.interestOps(0);

        CompleteAttachment remoteChannelAttachment = new CompleteAttachment(KeyState.FINISH_REMOTE_CONNECT, false);
        remoteChannelAttachment.setIn(attachment.getOut());
        remoteChannelAttachment.setOut(attachment.getIn());

        // register remote channel on connect to finish the connection
        remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT);
    }
}
