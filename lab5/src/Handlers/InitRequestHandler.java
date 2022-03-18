package Handlers;

import Attachments.CompleteAttachment;
import Attachments.BaseAttachment.KeyState;
import SOCKS.SOCKSv5;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class InitRequestHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws Exception {
        assert key != null;
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;
        assert key.attachment() instanceof CompleteAttachment;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        ByteBuffer buffer = attachment.getIn();

        if (clientChannel.read(buffer) == -1) {
            buffer.flip();
            // end-of-stream reached
            SOCKSv5.parseInitRequest(Arrays.copyOf(buffer.array(), buffer.limit()));    // exception can be thrown
            buffer.clear();
        }
        // set new state to the channel:
        key.interestOps(SelectionKey.OP_WRITE);
        attachment.setState(KeyState.INIT_RESPONSE);
    }
}
