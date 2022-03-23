package Handlers;

import Attachments.DnsAttachment;
import DNS.DomainNameResolver;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Arrays;

public class DnsResponseHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws Exception {
        SelectableChannel channel = key.channel();
        assert channel instanceof DatagramChannel;

        DatagramChannel dnsChannel = (DatagramChannel) channel;
        assert key.attachment() instanceof DnsAttachment;

        DnsAttachment attachment = (DnsAttachment) key.attachment();
        ByteBuffer buffer = attachment.getBuffer();

        if (dnsChannel.receive(buffer) == null) {
            return;                                 // no datagram immediately available
        }
        buffer.flip();
        byte[] respData = Arrays.copyOfRange(buffer.array(), 0, buffer.limit());
        buffer.clear();

        DomainNameResolver resolver = DomainNameResolver.getResolver();
        resolver.parseResponse(respData);
    }
}
