package Handlers;

import Attachments.CompleteAttachment;
import Attachments.BaseAttachment.KeyState;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;
import Exceptions.SocksException;
import SOCKS.SOCKSv5;

import java.io.IOException;
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

        // read data from channel
        int count;
        try {count = clientChannel.read(buffer);}
        catch (IOException e) {
            throw new HandlerException(Classes.INIT_RQST, "exception while reading data from channel", Types.IO);
        }
        // check end-of-stream was reached
        if (count == -1) {
            buffer.flip();
            try {
                SOCKSv5.parseInitRequest(Arrays.copyOf(buffer.array(), buffer.limit()));    // exception can be thrown
            }
            catch (SocksException e) {
                // set new state to the channel:
                key.interestOps(SelectionKey.OP_WRITE);
                attachment.setState(KeyState.INIT_RESPONSE_FAILED);
                throw e;
            }
            buffer.clear();
            // set new state to the channel:
            key.interestOps(SelectionKey.OP_WRITE);
            attachment.setState(KeyState.INIT_RESPONSE_SUCCESS);
        }
    }
}
