package Handlers;

import Attachments.CompleteAttachment;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

// read data from channel and write it to out buffer
public class ProxyingHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws Exception {
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        ByteBuffer buffer;

        if (key.isReadable()) {
            buffer = attachment.getOut();
            if (clientChannel.read(buffer) == -1) {
                // EOS reached so close channel
                HandlerFactory.getChannelCloser().handle(key);
            }
            else {
                key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);           // remove read operation
            }
            SelectionKey remoteKey = CompleteAttachment.getRemoteChannelKey(key);
            remoteKey.interestOps(remoteKey.interestOps() | SelectionKey.OP_WRITE); // add write operation to remote channel
            buffer.flip();
        }
        else if (key.isWritable()) {
            buffer = attachment.getIn();
            
        }
        else {
            assert false;
        }
    }
}
