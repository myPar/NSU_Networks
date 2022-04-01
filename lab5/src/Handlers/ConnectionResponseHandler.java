package Handlers;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;
import Logger.GlobalLogger;
import Logger.LogWriter;
import SOCKS.SOCKSv5;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectionResponseHandler implements Handler {
    private static GlobalLogger workflowLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.WORKFLOW_LOGGER);

    @Override
    public void handle(SelectionKey key) throws HandlerException {
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();

        String hostInfo = attachment.getRemoteAddress().getHostString() + " " + attachment.getRemoteAddress().getPort();
        LogWriter.logWorkflow("sending connection response. host: " + hostInfo, workflowLogger);

        ByteBuffer buffer = attachment.getIn();

        // write response to buffer (if don't wrote yet)
        if (!attachment.isRespWroteToBuffer) {
            byte[] responseData = SOCKSv5.getConnectionResponse(attachment);
            buffer.put(responseData);
            buffer.flip();
            attachment.isRespWroteToBuffer = true;
        }
        // write data to channel
        try {clientChannel.write(buffer);}
        catch (IOException e) {
            throw new HandlerException(Classes.CONNECTION_RESPONSE, "can't write response to channel", Types.IO);
        }
        // change channel state if message wrote
        if (buffer.remaining() <= 0) {
            LogWriter.logWorkflow("connection response send; host: " + hostInfo, workflowLogger);
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
                throw new HandlerException(Classes.CONNECTION_RESPONSE, "connection failed, stop the server", Types.FINISH);
            }
            else {assert false;}
        }
    }
}
