package Handlers;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import SOCKS.SOCKSv5;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class InitResponseHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws Exception {
        assert key != null;
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        ByteBuffer buffer = attachment.getOut();
        // write response to buffer
        if (attachment.getState() == KeyState.INIT_RESPONSE_FAILED) {
            buffer.put(SOCKSv5.getFailedInitResponse());
        }
        else if(attachment.getState() == KeyState.INIT_RESPONSE_SUCCESS) {
            buffer.put(SOCKSv5.getSuccessInitResponse());
        }
        else {
            assert false;
        }
    }
}
