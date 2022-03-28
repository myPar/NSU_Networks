package Handlers;

import Attachments.BaseAttachment;
import Attachments.CompleteAttachment;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;
import Logger.GlobalLogger;
import Logger.LogWriter;

import java.io.IOException;
import java.nio.channels.*;

public class AcceptHandler implements Handler {
    private static GlobalLogger workflowLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.WORKFLOW_LOGGER);
    @Override
    public void handle(SelectionKey key) throws HandlerException {
        assert key != null;
        try {
            LogWriter.logWorkflow("accepting new client..", workflowLogger);

            SelectableChannel channel = key.channel();
            assert channel instanceof ServerSocketChannel;

            ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
            Selector selector = key.selector();

            //accept new client channel
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            // register channel on read initial request
            clientChannel.register(selector, SelectionKey.OP_READ, new CompleteAttachment(BaseAttachment.KeyState.INIT_REQUEST, true));

            LogWriter.logWorkflow("new client accepted", workflowLogger);
        }
        catch (IOException e) {
            throw new HandlerException(Classes.ACCEPT, "can't accept new client", Types.IO);
        }
    }
}
