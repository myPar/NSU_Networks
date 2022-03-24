package Handlers;

import Attachments.BaseAttachment.KeyState;
import Attachments.CompleteAttachment;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class FinishConnectionHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws Exception {
        SelectableChannel channel = key.channel();
        assert channel instanceof SocketChannel;

        SocketChannel clientChannel = (SocketChannel) channel;
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();

        // get remote channel and key
        Selector selector = key.selector();
        SelectableChannel remoteChannel = attachment.getRemoteChannel();
        SelectionKey remoteKey = remoteChannel.keyFor(selector);
        try {
            clientChannel.finishConnect();
        }
        catch (IOException e) {
            registerOnConnectionResponse(KeyState.CONNECT_RESPONSE_FAILED, remoteKey);
            String msg = "can't finish the connection to " + attachment.getRemoteAddress().getHostString() + " " + attachment.getRemoteAddress().getPort();
            throw new HandlerException(Classes.FINISH_CONNECTION, msg, Types.IO);
        }
        key.interestOps(0);

        // register remote key on Connection response success
        registerOnConnectionResponse(KeyState.CONNECT_RESPONSE_SUCCESS, remoteKey);
    }
    private void registerOnConnectionResponse(KeyState state, SelectionKey key) {
        key.interestOps(SelectionKey.OP_WRITE);
        CompleteAttachment attachment = (CompleteAttachment) key.attachment();
        assert state == KeyState.INIT_RESPONSE_FAILED || state == KeyState.INIT_RESPONSE_SUCCESS;
        attachment.setState(state);
    }
}
