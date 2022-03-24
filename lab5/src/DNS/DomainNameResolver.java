package DNS;

import Attachments.CompleteAttachment;
import Core.Constants.ResolverConstants;
import Exceptions.ResolverException;
import Exceptions.ResolverException.Classes;
import Exceptions.ResolverException.Types;
import Handlers.HandlerFactory;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
 Resolver provides methods for Main Thread which handle channels, and methods for it's own thread
 Main Thread: put new domain name on resolving and parse dns-server response
 Own thread: send datagrams with domain name resolve requests with pre set time interval
*/
public class DomainNameResolver extends Thread {
    // port + channel key
    public static class KeyData {
        public final int port;
        public final SelectionKey key;

        public KeyData(int port, SelectionKey key) {
            this.port = port;
            this.key = key;
        }
    }
    // key - domain name; value - list of key data
    private HashMap<String, List<KeyData>> unresolvedDomainNameMap;
    // key - domain name; value - resolved ip address (string representation)
    private HashMap<String, String> resolvedDomainNameMap;

    // channel for sending and receiving dns datagrams
    private DatagramChannel resolverChannel;
    // address of available dns server
    private final InetSocketAddress dnsServerAddress;

    private boolean stopped = false;

    // private constructor for single-ton
    private DomainNameResolver(DatagramChannel channel) {
        resolverChannel = channel;
        resolvedDomainNameMap = new HashMap<>(ResolverConstants.CACHE_CAPACITY);
        unresolvedDomainNameMap = new HashMap<>();

        List<InetSocketAddress> dnsServers = ResolverConfig.getCurrentConfig().servers();
        dnsServerAddress = dnsServers.get(0);
    }
    // single-ton helper:
    private static DomainNameResolver resolver;
    public static void createResolver(DatagramChannel channel) {
        if (resolver == null) {
            resolver = new DomainNameResolver(channel);
        }
    }
    public static DomainNameResolver getResolver() {
        assert resolver != null;
        return resolver;
    }

//methods called by Own Thread:
    // sending dn-resolving requests for all unresolved names
    private synchronized void sendRequests() throws Exception {
        if (unresolvedDomainNameMap.size() <= 0) {
            wait();
        }
        Set<String> keySet = unresolvedDomainNameMap.keySet();
        for(String key: keySet) {
            sendRequest(key);
        }
    }

    // method of sending domain name resolving datagrams
    private void sendRequest(String domainName) throws ResolverException {
        try {
            Message request = createDNSmessage(domainName);
            byte[] requestBytes = request.toWire();
            resolverChannel.send(ByteBuffer.wrap(requestBytes), dnsServerAddress);
        } catch (TextParseException e) {
            throw new ResolverException(Classes.REQUEST, "invalid domain name", Types.FORMAT);
        }
        catch (IOException e) {
            throw new ResolverException(Classes.REQUEST, "can't send the request", Types.IO);
        }
    }

    private Message createDNSmessage(String name) throws TextParseException {
        Message result = new Message();

        Header header = new Header();
        header.setFlag(Flags.RD);                   // recursion desired
        header.setOpcode(Opcode.QUERY);             // standard dns-query

        Record record = Record.newRecord(new Name(name + "."), Type.A, DClass.IN);  // Type - 'Address', Class - 'Internet'
        result.setHeader(header);
        result.addRecord(record, Section.QUESTION); // write record in 'Question' section of the message

        return result;
    }

    // Resolver Thread send's dns datagrams for unresolved names
    @Override
    public void run() {
        while(!stopped) {
            try {
                sendRequests();
                Thread.sleep(ResolverConstants.REQUESTS_DELTA_TIME);    // Interrupted exception
            } catch (Exception e) {
                // TODO log fatal exception
            }
        }
    }
//methods called bt Main Thread:
    // main method
    public void resolveDomainName(String name, KeyData keyData) throws Exception {
        if (resolvedDomainNameMap.containsKey(name)) {
            String address = resolvedDomainNameMap.get(name);
            assert keyData.key.attachment() instanceof CompleteAttachment;

            // address is resolved so can establish the connection to remote channel
            CompleteAttachment attachment = (CompleteAttachment) keyData.key.attachment();
            attachment.setRemoteAddress(AddressGetter.getAddress(address, keyData.port));

            HandlerFactory.getConnector().connectToChannel(keyData.key);
        }
        else {
            putUnresolvedItem(name, keyData);
        }
    }

    // put new KeyData item for resolving
    private synchronized void putUnresolvedItem(String dn, KeyData keyData) {
        if (!unresolvedDomainNameMap.containsKey(dn)) {
            List<KeyData> list = new LinkedList<>();
            list.add(keyData);
            unresolvedDomainNameMap.put(dn, list);
        }
        else {
            unresolvedDomainNameMap.get(dn).add(keyData);
        }
        notify();
    }

    // parse dns-server response
    public synchronized void parseResponse(byte[] responseData) throws ResolverException {
        try {
            Message response = new Message(responseData);
            List<Record> answers = response.getSection(Section.ANSWER);

            if (answers.size() == 0) {
                throw new ResolverException(Classes.RESPONSE, "empty answer", Types.FORMAT);
            }
            // check is host name has already been resolved
            String hostName = response.getQuestion().getName().toString();
            if (resolvedDomainNameMap.containsKey(hostName)) {
                return;// just rop datagram
            }
            String address = answers.get(0).rdataToString();

            // domain name is resolved so connect channels, remove it from 'unresolved' map and add to 'resolved' map:
            connectAllChannels(hostName, address);
            unresolvedDomainNameMap.remove(hostName);
            resolvedDomainNameMap.put(hostName, address);
        }
        catch (IOException e) {
            throw new ResolverException(Classes.RESPONSE, "can't parse the dns server response", Types.IO);
        }
    }

    // connect all channels with resolved domain name
    private void connectAllChannels(String domainName, String address) throws ResolverException {
        assert unresolvedDomainNameMap.containsKey(domainName);

        List<KeyData> list = unresolvedDomainNameMap.get(domainName);

        for (KeyData item : list) {
            // set remote address in key attachment and connect channel:
            InetSocketAddress dstAddress = new InetSocketAddress(address, item.port);
            CompleteAttachment attachment = (CompleteAttachment) item.key.attachment();
            attachment.setRemoteAddress(dstAddress);
            try {
                HandlerFactory.getConnector().connectToChannel(item.key);
            }
            catch (Exception e) {
                String msg = "can't connect to resolved address '" + domainName + "'" + " - " + address;
                throw new ResolverException(Classes.RESPONSE, msg, Types.CONNECT);
            }
        }
    }

    public void stopResolver() {
        this.stopped = true;
    }
}
