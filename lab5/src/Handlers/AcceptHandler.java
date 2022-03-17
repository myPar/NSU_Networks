package Handlers;

import Attachments.BaseAttachment;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;

import java.io.IOException;
import java.nio.channels.*;

public class AcceptHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws HandlerException {
        try {
            SelectableChannel channel = key.channel();
            assert channel instanceof ServerSocketChannel;

            ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
            Selector selector = key.selector();

            //accept new client channel
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            // register channel on read initial request
            clientChannel.register(selector, SelectionKey.OP_READ, new BaseAttachment(BaseAttachment.KeyState.INIT_REQUEST));
        }
        catch (IOException e) {
            throw new HandlerException(Classes.ACCEPT, "can't accept new client", Types.IO);
        }
    }
}
