# lab5 - SOCKSv5 Proxy
## Lab Description
_Proxy server of standart SOCKS (version 5) which supports one command - "establish a TCP/IP stream connection". Handling of several clients is implemented using default Java-API (see: Selector, SelectionKey, SelectableChannel etc. in Java API references)._

### Proxying
A little about proxying:
1. Server accepts new Client
2. Client sends to server a 'connection request' which contains dst-host address and port.
3. Server establishes a connection to dst host.
4. Proxying starts - server implements data traversing between dst-host and client.

### SOCKSv5 protocol
See full protocol description [here](https://www.ietf.org/rfc/rfc1928.txt) or on [wikipedia](https://ru.wikipedia.org/wiki/SOCKS). 
1. The Client sends "Hello" message to Server, which contains protocol version (5 in our case), and numbers of supported authentication methods. 

2. Server responses with message which contains protocol version and number of choosen authentication method. 
3. Then the Client sends 'Connection request' message which contains 'command code' (0x01 - 'establish TCP/IP connection' in our case), 'dst address type', 'dst address value' and 'dst port number'. 
4. Server established connection to dst host and sends response to the Client - with the same data as in client request except that instead of 'command code' there is 'response code' (0x00 in casse of success)
5. Then proxying starts

___
<a href="https://imgbb.com/"><img src="https://i.ibb.co/pvF0rsH/sequence.png" alt="sequence" border="0"></a>
___

### Components
__Server__ - Initialize ServerSocketChannel instance wich will be used for accepting new clients. Contains main client handling method - _handleClients()_ where calls _select()_ method and handle channels whose keys were returned by this method.
__HandlerFactory__ - depending on the channel state _getHandler(SelectionKey key)_ method returns corresponding handler as an instance of the _Handler_ interface. For channel's handling - the _handle(SelectionKey key)_ method is invoked.
__SOCKSv5__ - contains constans of SOCKSv5 protocol; also contains methods for parsing and getting the messages data: _InitRequest_, _InitResponse_, _ConnectRequest_ and _ConnectResponse_ messages
__BaseAttachment__ - Base key attachment class. Contains definition of key states (enum) and state field (for object). Using in _getHandler()_ method of ___HandlerFactory___ class. And anywhere where just channel state is needed.
__CompleteAttachment__ - more complex type of attachment. Extended the ___BaseAttachment___ class. Contains input/output ByteBuffer object for sending and receiving data. Also contains link to remote channel. Some info about 'connection request' and dst host address. Needed when keys with states  _ConnectionRequest_, _ConnectionResponse_, _Proxying_ and _FinishConnection_ are handled.
__Handler__ - interface which contains only one method - _handle(SelectionKey)_. Handlers of all types are implement it.
___
<a href="https://ibb.co/6tDbNzf"><img src="https://i.ibb.co/qgm9xKP/uml.png" alt="uml" border="0"></a><br /><a target='_blank' href='https://ru.imgbb.com/'></a><br />
___

### Workflow
+ Server starts and initialize ServerSocketChannel which will be accepting new clients. Server register it in the selector on _OP_ACCEP_.
+ Accepted ClientChannel is registered on _OP_READ_ to read the init request (see SOCKS protocol description). 
+ In main method _handleClients()_ method _select()_ is invoked which returns keys of channels which are ready for the operations. _Handler_ instance is returned by invoking of the method _HandlerFactory.getHandler()_ and _handle()_ method is called to handle the key.
+ Key is changing the states while throughout the passage of the workflow. There is a separate handler class for each key state.
+ When proxying is complete (___EOS___ is reached while reading data from the client or remote channels) the channels are closed.
+ If any error occures all channels are closed and the server is interrupted. Except the case when SOCKS error accurs - in this case the _error response_ is send to client and only after that the server is interrupted.

### Key states
```java
    public enum KeyState {
        ACCEPT,                     // server channel state - accepting new clients
        INIT_REQUEST,               // client channel state - reading 'Hello' msg from channel
        INIT_RESPONSE_SUCCESS,      // client channel state - writing 'Hello' msg response to channel
        INIT_RESPONSE_FAILED,       // client channel state - writing failed response on 'Hello' msg to channel
        CONNECT_REQUEST,            // client channel state - reading 'Connection request' msg from channel
        CONNECT_RESPONSE_SUCCESS,   // client channel state - writing success connection response to channel
        CONNECT_RESPONSE_FAILED,    // client channel state - writing failed connection response to channel
        FINISH_REMOTE_CONNECT,      // remote channel state - finish the connection of remote channel
        DNS_RESPONSE,               // datagram channel state - getting response from dns server
        PROXYING                    // proxying state
    }
```