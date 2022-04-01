package Handlers;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import Exceptions.HandlerException;
import Logger.GlobalLogger;
import Logger.LogWriter;
import SOCKS.SOCKSv5;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class InitResponseHandler implements Handler {
    private static GlobalLogger workflowLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.WORKFLOW_LOGGER);

    @Override
    public void handle(SelectionKey key) throws HandlerException {
        LogWriter.logWorkflow("sending init response..", workflowLogger);

        assert key != null;
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        ByteBuffer buffer = attachment.getIn();

        // write response to buffer (if don't wrote yet)
        if (!attachment.isRespWroteToBuffer) {
            if (attachment.getState() == KeyState.INIT_RESPONSE_FAILED) {
                buffer.put(SOCKSv5.getFailedInitResponse());
            } else if (attachment.getState() == KeyState.INIT_RESPONSE_SUCCESS) {
                buffer.put(SOCKSv5.getSuccessInitResponse());
            } else {
                assert false;
            }
            attachment.isRespWroteToBuffer = true;
            buffer.flip();
        }
        // write data to channel
        try {clientChannel.write(buffer);}
        catch (IOException e) {
            throw new HandlerException(Classes.INIT_RESPONSE, "can't write data to channel", Types.IO);
        }
        // change channel state if message wrote
        if (buffer.remaining() <= 0) {
            LogWriter.logWorkflow("init response send", workflowLogger);

            if (attachment.getState() == KeyState.INIT_RESPONSE_SUCCESS) {
                attachment.isRespWroteToBuffer = false; // reset flag
                buffer.clear();
                // now register key on reading a connection request
                attachment.setState(KeyState.CONNECT_REQUEST);
                key.interestOps(SelectionKey.OP_READ);
            }
            else if (attachment.getState() == KeyState.INIT_RESPONSE_FAILED) {
                throw new HandlerException(Classes.INIT_RESPONSE, "request was invalid, stop the server", Types.FINISH);
            }
            else {assert false;}
        }
    }
}
