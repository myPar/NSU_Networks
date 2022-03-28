package Handlers;

import Attachments.DnsAttachment;
import DNS.DomainNameResolver;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Types;

import java.io.IOException;
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

        // receive the datagram
        try {
            if (dnsChannel.receive(buffer) == null) {
                return;                                 // no datagram immediately available
            }
        }
        catch (IOException e) {
            throw new HandlerException(HandlerException.Classes.DNS_RESPONSE, "can't receive the datagram", Types.IO);
        }
        buffer.flip();
        byte[] respData = Arrays.copyOfRange(buffer.array(), 0, buffer.limit());
        buffer.clear();

        DomainNameResolver resolver = DomainNameResolver.getResolver();
        resolver.parseResponse(respData);
    }
}
