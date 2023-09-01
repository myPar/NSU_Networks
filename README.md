# lab5 - SOCKSv5 Proxy
## Lab Description
_This project implements a proxy server based on SOCKS v5 protocol, which can serve multiple clients and which supports a single command "establish a TCP/IP stream connection". Implementation uses only non-blocking sockets and it works with them in __one Thread__, it uses standard Java APIs for this purpose - Selector, SelectionKey, SelectableChannel etc. This API provides handling several socket channels iteratively using only one thread. Channels are handled only when they are ready for the operations, so the thread doesn't blocked to handle the channel. If you are new to use non-blocking sockets in Java I recoment to check [this article](https://www.developer.com/java/data/what-is-non-blocking-socket-programming-in-java/) and [Java documentation](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/Selector.html)._

### Proxying
The main scenario of the proxy server interacting with clients is described below. The server and clients can reside anywhere in a world-wide-web. Client should be configured to use the proxy. In particular you must configure proxy's IP-address and proxy's port in your browser if you want to use a proxy through it.
1. Server accepts new Client
2. Client sends to server a 'connection request' which contains dst-host address and port.
3. Server establishes a connection to dst host.
4. Proxying starts - server implements data tunneling between dst-host and the client.

### SOCKSv5 protocol
In this project the medium for communication is the SOCKSv5 protocol, which is overviewed briefly below. See full protocol description [here](https://www.ietf.org/rfc/rfc1928.txt) or on [wikipedia](https://ru.wikipedia.org/wiki/SOCKS).
1. The Client sends "Hello" message to Server which contains supported authentication methods.

2. Server responses with message wich contains choosen authentication method. 
3. Then the Client sends 'Connection request' message which contains command he wants the server to execute (in our case this is _'establish a TCP/IP stream connection'_) and destination host info - address, port and the type of the address (domain name, IPv4 or IPv6).
4. Server executes command (establishes connection to dst host in our case) and sends response to the Client - with the same data as in client's request except that instead of command there is a response status - ither 'success' status or exception info.
5. If no exception was occured proxying starts.

___
<a href="https://imgbb.com/"><img src="https://i.ibb.co/pvF0rsH/sequence.png" alt="sequence" border="0"></a>
___

### Workflow
Here are the stages of server's workflow. Each stage has its own detailed description below.
1. [Server initializing](#server-initializing).
2. [Accepting new client](#accepting-new-client).
3. [Handle accepted clients](#handle-accepted-clients).
4. [Proxying](#proxying)
5. [Сompletion of client handling](#completion-of-client-handling).

#### Server Initializing
Server starts and initialize ServerSocketChannel. This channel is registered in the selector on [OP_ACCEPT](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SelectionKey.html). Then _handleClients()_ method invokes. This method contains an infinite loop where the _select()_ method of the selector call occurs. _select()_ method returns key-set of the registered in selector channels which are ready for the operations. At server start only ServerSocketChannel is registered in the selector.
#### Accepting new Client
When ServerSocketChannel is ready for the operation (it can be only OP_ACCEPT) the _handle()_ method of _AcceptHandler_ class invokes with the key of the ServerSocketChannel. In this method non bocking accept operation is invoked which returns SocketChannel of the accepted client. This channel is registered in the selector on [OP_READ](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SelectionKey.html). Now 'Hello' message is expected to be read from this channel.
#### Handle accepted clients
Each SocketChannel of the accepted client has it's [state](#channel-states). Right after client channel is registered in the selector he has _INIT_REQUEST_ state. Through the workflow client channel changes it's state. At each iteration of the server's _handleClients()_ method, a _Handler_ interface instance is taken for each channel ready for operation. This instance is an object of Handler class. There is single Handler class for each channel state. All of these classes implements _Handler_ interface.
So depending on the channel state the Handler class object is taken and _handle()_ method of this object invokes to handle this channel.
####  Proxying
The final channel state is proxying when server provides data tuneling between client channel and remote channel. Here is the description of how does it implemented. During the handling of the 'Connection request' of the client new SocketChannel is created and registered in the selector on [OP_CONNECT](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SelectionKey.html), this is the remote channel. The ConnectionHandler finishes the connection of the remote channel in it's _handle()_ method and after that the client channel is registered on [OP_WRITE](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SelectionKey.html) to get 'Connection Response' from server. When response is wrote server starts data tunelling between client channel and remote channel. Tunneling mechanizm describes below. 

Client channel and remote channel has two same buffers - first one is the input buffer for client channel and output buffer for remote channel and the second one is output buffer for client channel and input buffer for the remote channel (see scheme below). At proxying start both channels has OP_READ as the interested operation. 

When one of the channels are handled on OP_READ (data was read from it) read data is written to channel's output buffer. When one of the channels are handled on OP_WRITE data writes to channel from it's input buffer. Then channels' interest operations are changed like this: At any point of time current operation of the handled channel is removed and opposite (OP_READ has OP_WRITE as an opposite and vise versa) operation  is added to another channel. So no one of buffers can be used by both channels simultaneously.

<a href="https://imgbb.com/"><img src="https://i.ibb.co/1Q0Vvkt/channels.png" alt="channels" border="0"></a><br /><a target='_blank' href='https://imgbb.com/'></a><br />

#### Completion of Client handling
When end-of-stream reached in one of the channels the remaining data is written to another channel and both channels are closed - their keys became canceled in the selector and _close()_ method for the channels is invoked.

### Components
__Server__ - Initialize ServerSocketChannel instance. Contains _handleClients()_ method where selector's _select()_ method invokes.

__HandlerFactory__ - Provides handlers each channel [state](#channel-states) via _getHandler(SelectionKey key)_ method. This method, depending on the channel state, returns corresponding handler as an instance of the _Handler_ interface. Each handler processes channel via the _handle(SelectionKey key)_ method.

__SOCKSv5__ - contains constans of SOCKSv5 protocol; also contains methods for parsing and getting the messages data: _InitRequest_, _InitResponse_, _ConnectRequest_ and _ConnectResponse_ messages

__BaseAttachment__ - _key attachment_ is an object which contains some usefull information about the channel this key refers. This is base key attachment class. Contains definition of channels' [states](#channel-states) and state field (for object). Used in _getHandler()_ method of ___HandlerFactory___ class. And anywhere where just channel state is needed.

__CompleteAttachment__ - more complex type of attachment. Extended the ___BaseAttachment___ class. Contains input/output ByteBuffer object for sending and receiving data. Also contains link to remote channel. Some info about 'connection request' and dst host address. Needed when keys with states  _ConnectionRequest_, _ConnectionResponse_, _Proxying_ and _FinishConnection_ are handled.

__Handler__ - interface which contains only one method - _handle(SelectionKey)_. Handlers of all types are implement it.
___
<a href="https://ibb.co/6tDbNzf"><img src="https://i.ibb.co/qgm9xKP/uml.png" alt="uml" border="0"></a><br /><a target='_blank' href='https://ru.imgbb.com/'></a><br />
___

### Channel states
There channel states are describes.  
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