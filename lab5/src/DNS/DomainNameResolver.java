package DNS;

import java.nio.channels.SelectionKey;
import java.util.HashMap;

public class DomainNameResolver {
    // key - domain name; value - key of client channel, which send connection request to host with this domain name
    private HashMap<String, SelectionKey> unresolvedDomainNameMap;
    // key - domain name; value - resolved ip address
    private HashMap<String, String> resolvedDomainNameMap;

    
}
