/*
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class Socks4Proxy implements Runnable {
    int bufferSize = 8192;
    int port;
    String host;

    static class Attachment {

        ByteBuffer in;

        ByteBuffer out;

        SelectionKey peer;

    }

    static final byte [] OK = new byte [] {0x00, 0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    @Override
    public void run () {
        try {
            // Create Selector
            Selector selector = SelectorProvider.provider (). openSelector ();
            // Open the server channel
            ServerSocketChannel serverChannel = ServerSocketChannel.open ();
            // Remove the lock
            serverChannel.configureBlocking (false);
            // Hang on the port
            serverChannel.socket ().bind(new InetSocketAddress (host, port));
            // Registration in the
            serverChannel.register(selector, serverChannel.validOps());
            // The main loop the operation of a non-blocking server
            // This cycle will be the same for almost any non-blocking
            // server
            while (selector.select() > -1) {
                // Get the keys on which events occurred at the moment
                // last
                iterator fetch iterator = selector.selectedKeys (). iterator ();
                while (iterator.hasNext ()) {
                    SelectionKey key = iterator.next ();
                    iterator.remove();
                    if (key.isValid ()) {
                        // Handle all possible key events
                        try {
                            if (key.isAcceptable ()) {
                                // Accept the connection
                                accept (key);
                            } else if (key.isConnectable ()) {
                                // Make a connection
                                connect (key);
                            } else if (key.isReadable ()) {
                                // Read data
                                read (key);
                            } else if (key.isWritable ()) {
                                // Write data
                                write (key);
                            }
                        } catch (Exception e) {
                            e.printStackTrace ();
                            close (key);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace ();
            throw new IllegalStateException (e);
        }
    }

    private void accept (SelectionKey key) throws IOException, ClosedChannelException {
        // Accepted
        SocketChannel newChannel = ((ServerSocketChannel) key.channel ()).accept ();
        // Non-blocking
        newChannel.configureBlocking(false);
        // Register in the selector
        newChannel.register(key.selector(), SelectionKey.OP_READ);
    }

    private void read (SelectionKey key) throws IOException, UnknownHostException, ClosedChannelException {
        SocketChannel channel = ((SocketChannel) key.channel ());
        Attachment attachment = ((Attachment) key.attachment ());
        if (attachment == null) {
            // Lazily initialize the buffers
            key.attach (attachment = new Attachment ());
            attachment.in = ByteBuffer.allocate (bufferSize);
        }
        if (channel.read(attachment.in) < 1) {
            // -1 - gap 0 - there is no space in the buffer, this can only happen if the
            // header exceeds the buffer size
            close (key);
        } else if (attachment.peer == null) {
            // if there is no second end :) so we read the header
            readHeader(key, attachment);
        } else {
            // well, if we proxy, then we add interest to the second end
            // write
            attachment.peer.interestOps(attachment.peer.interestOps () | SelectionKey.OP_WRITE);
            // and we remove the interest from the first one to read, because we have not written
            // the current data, we will not read anything
            key.interestOps (key.interestOps () ^ SelectionKey.OP_READ);
            // prepare a buffer for writing
            attachment.in.flip();
        }
    }

    private void readHeader (SelectionKey key, Attachment attachment) throws IllegalStateException, IOException,
            UnknownHostException, ClosedChannelException {
        byte[] ar = attachment.in.array();
        if (ar[attachment.in.position() - 1] == 0) {
            // If the last byte \ 0 is the end of the user ID.
            if (ar[0] != 4 && ar [1] != 1 || attachment.in.position () <8) {
                // A simple check on the protocol version and on the validity of
                // commands,
                // We only support conect
                throw new IllegalStateException ("Bad Request");
            } else {
                // Create a connection
                SocketChannel peer = SocketChannel.open();
                peer.configureBlocking (false);
                // Get the address and port from the packet
                byte [] addr = new byte [] {ar[4], ar[5], ar[6], ar[7]};
                int p = (((0xFF & ar [2]) << 8) + (0xFF & ar [3]));
                // We begin to establish a connection
                peer.connect(new InetSocketAddress(InetAddress.getByAddress(addr), p));
                // Registration in the
                SelectionKey peerKey = peer.register(key.selector(), SelectionKey.OP_CONNECT);
                // Mute the requesting connection
                key.interestOps(0);
                // Key exchange :)
                attachment.peer = peerKey;
                Attachment peerAttachment = new Attachment ();
                peerAttachment.peer = key;
                peerKey.attach(peerAttachment);
                // Clear the buffer with headers
                attachment.in.clear();
            }
        }
    }

    private void write (SelectionKey key) throws IOException {
        // Close the socket only by writing all the data
        SocketChannel channel = ((SocketChannel) key. channel ());
        Attachment attachment = ((Attachment) key.attachment ());
        if (channel.write(attachment.out) == -1) {
            close (key);
        } else if (attachment.out.remaining() == 0) {
            if (attachment.peer == null) {
                // Add what was in the buffer and close
                close (key);
            } else {
                // if everything is recorded, clear the
                attachment.out.clear();
                // Add to the second end the interest in reading
                attachment.peer.interestOps (attachment.peer.interestOps() | SelectionKey.OP_READ);
                // And we remove our interest in writing
                key.interestOps(key.interestOps () ^ SelectionKey.OP_WRITE);
            }
        }
    }

    private void connect (SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        Attachment attachment = ((Attachment) key.attachment());
        // End the connection
        channel.finishConnect();
        // Create a buffer and respond OK
        attachment.in = ByteBuffer.allocate (bufferSize);
        attachment.in.put (OK).flip();
        attachment.out = ((Attachment) attachment.peer.attachment()).in;
        ((Attachment) attachment.peer.attachment ()).out = attachment.in;
        // Set the second end of the flags for writing and reading
        // as soon as she writes OK, switches the second end to reading and all
        // will be happy
        attachment.peer.interestOps (SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        key.interestOps (0);
    }
    private void close (SelectionKey key) throws IOException {
        key.cancel ();
        key.channel ().close ();
        SelectionKey peerKey = ((Attachment) key.attachment ()).peer;
        if (peerKey != null) {
            ((Attachment) peerKey.attachment ()).peer = null;
            if ((peerKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                ((Attachment) peerKey.attachment ()). out.flip ();
            }
            peerKey.interestOps (SelectionKey.OP_WRITE);
        }
    }

    public static void main (String [] args) {
        Socks4Proxy server = new Socks4Proxy ();
        server.host = "127.0.0.1";
        server.port = 1080;
        server.run ();
    }
}
*/