package Handlers;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import Logger.GlobalLogger;
import Logger.LogWriter;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class FinishConnectionHandler implements Handler {
    private static GlobalLogger workflowLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.WORKFLOW_LOGGER);

    @Override
    public void handle(SelectionKey key) throws Exception {
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();

        SelectionKey remoteKey = CompleteAttachment.getRemoteChannelKey(key);
        CompleteAttachment remoteAttachment = (CompleteAttachment) remoteKey.attachment();

        String hostInfo = remoteAttachment.getRemoteAddress().getHostString() + " " + remoteAttachment.getRemoteAddress().getPort();
        LogWriter.logWorkflow("finishing connection to host: " + hostInfo, workflowLogger);

        try {
            clientChannel.finishConnect();
        }
        catch (IOException e) {
            registerOnConnectionResponse(KeyState.CONNECT_RESPONSE_FAILED, remoteKey);
            String msg = "can't finish the connection to " + attachment.getRemoteAddress().getHostString() + " " + attachment.getRemoteAddress().getPort();
            LogWriter.logWorkflow(msg, workflowLogger);
            return;
        }
        key.interestOps(0);
        // register remote key on Connection response success
        registerOnConnectionResponse(KeyState.CONNECT_RESPONSE_SUCCESS, remoteKey);

        LogWriter.logWorkflow("connection finished to host " + hostInfo, workflowLogger);
    }
    private void registerOnConnectionResponse(KeyState state, SelectionKey key) {
        key.interestOps(SelectionKey.OP_WRITE);
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        assert state == KeyState.CONNECT_RESPONSE_FAILED || state == KeyState.CONNECT_RESPONSE_SUCCESS;
        attachment.setState(state);
    }
}
