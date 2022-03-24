package Handlers;

import Attachments.CompleteAttachment;
import Attachments.ConnectionRequestData;

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


    }
}
