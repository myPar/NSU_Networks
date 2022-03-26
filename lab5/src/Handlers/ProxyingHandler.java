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
            SelectionKey remoteKey = CompleteAttachment.getRemoteChannelKey(key);
            if (clientChannel.read(buffer) == -1) {
                // EOS reached so close channel
                HandlerFactory.getChannelCloser().handle(key);
                remoteKey.interestOps(SelectionKey.OP_WRITE);   // we should write all remaining data
            }
            else {
                key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);           // remove read operation
                remoteKey.interestOps(remoteKey.interestOps() | SelectionKey.OP_WRITE); // add write operation to remote channel
            }
            buffer.flip();  // prepare buffer to writing
        }
        else if (key.isWritable()) {
            buffer = attachment.getIn();
            clientChannel.write(buffer);

            if (buffer.remaining() == 0) {
                if (attachment.getRemoteChannel() == null) {
                    // remote channel is closed, so close the connection
                    HandlerFactory.getChannelCloser().handle(key);
                }
                else {
                    // all data wrote so clear the buffer and change channel's states:
                    buffer.clear();

                    SelectionKey remoteKey = CompleteAttachment.getRemoteChannelKey(key);
                    remoteKey.interestOps(remoteKey.interestOps() | SelectionKey.OP_READ);
                    key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
                }
            }
        }
        else {
            assert false;
        }
    }
}
