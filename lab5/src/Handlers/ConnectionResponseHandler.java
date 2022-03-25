package Handlers;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import Attachments.ConnectionRequestData;
import SOCKS.SOCKSv5;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectionResponseHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws Exception {
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        ConnectionRequestData requestData = attachment.getConnectionRequestData();

        ByteBuffer buffer = attachment.getOut();

        // write response to buffer (if don't wrote yet)
        if (!attachment.isRespWroteToBuffer) {
            byte[] responseData = SOCKSv5.getConnectionResponse(attachment);
            buffer.put(responseData);
            buffer.flip();
            attachment.isRespWroteToBuffer = true;
        }
        clientChannel.write(buffer);

        // change channel state if message wrote
        if (buffer.remaining() <= 0) {
            attachment.isRespWroteToBuffer = false; // reset flag
            buffer.clear();

            if (attachment.getState() == KeyState.CONNECT_RESPONSE_SUCCESS) {
                // change client channel state
                attachment.setState(KeyState.PROXYING);
                key.interestOps(SelectionKey.OP_READ);

                SelectionKey remoteKey = CompleteAttachment.getRemoteChannelKey(key);
                CompleteAttachment remoteKeyAttachment = (CompleteAttachment) remoteKey.attachment();

                // change remote channel state
                remoteKeyAttachment.setState(KeyState.PROXYING);
                remoteKey.interestOps(SelectionKey.OP_READ);
            }
            else if (attachment.getState() == KeyState.CONNECT_RESPONSE_FAILED) {
                // TODO write close channel handling
            }
            else {assert false;}
        }
    }
}
