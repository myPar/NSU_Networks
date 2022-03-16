package Core;


import Attachments.BaseAttachment;
import Exceptions.ServerException;
import Handlers.HandlerFactory;
import Logger.GlobalLogger;
import Attachments.BaseAttachment.KeyState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import Handlers.Handler;
import java.util.logging.Level;
//TODO: write exceptions descriptions
//TODO: write logging

public class Server {
    // get global loggers references:
    private static GlobalLogger workflowLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.WORKFLOW_LOGGER);
    private static GlobalLogger exceptionLogger = GlobalLogger.LoggerCreator.getLogger(GlobalLogger.LoggerType.EXCEPTION_LOGGER);

    private static boolean started = false; // is server started (to prevent repeating execution)
    private static boolean stopped = false; // is server stopped (flag of exiting from handle while loop)

    private Selector selector;
    private int port;

    private Server(int port, GlobalLogger.Mode mode) throws ServerException {
        this.port = port;
        workflowLogger.setMode(mode);
        exceptionLogger.setMode(mode);
        // create selector and register Server channel and datagram channel:
        initServer();
    }
    // server init methods:
    private void initServer() throws ServerException {
        try {
            selector = Selector.open();
        } catch (IOException e) {throw new ServerException(null, "");}
        initServerSocketChannel();
        initDatagramChannel();
    }
    private void initServerSocketChannel() throws ServerException {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT, new BaseAttachment(KeyState.ACCEPT));
        }
        catch (IOException e) {
            throw new ServerException(null, "");
        }
    }
    private void initDatagramChannel() throws ServerException {
        try {
            DatagramChannel udpChannel = DatagramChannel.open();
            udpChannel.configureBlocking(false);
            udpChannel.register(selector, SelectionKey.OP_READ, new BaseAttachment(KeyState.DNS_RESPONSE));
        }
        catch (IOException e) {
            throw new ServerException(null, "");
        }
    }

    // main handle loop
    private void handleClients() {
        while(!Server.stopped) {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            SelectionKey curKey;
            while(keyIterator.hasNext()) {
                curKey = keyIterator.next();
                // remove key from set to prevent handling it twice (selector doesn't remove key by itself)
                keyIterator.remove();
                handle(curKey);
            }
        }
        closeChannels();
        // update flags
        Server.stopped = false;
        Server.started = false;
    }
    // handle single key method
    private void handle(SelectionKey key) {
        BaseAttachment attachment = (BaseAttachment) key.attachment();
        Handler keyHandler = HandlerFactory.getHandler(key);
        keyHandler.handle(key);
    }
    // close all channels
    private void closeChannels() {

    }

    // main server start method
    public static void start(int port, GlobalLogger.Mode mode) {
        if (!Server.started) {
            Server.started = true;
            Server instance = null;
            try {
                instance = new Server(port, mode);
            } catch (ServerException e) {
                Server.exceptionLogger.log(Level.WARNING, e.getBaseMessage());
                System.exit(1);
            }
            instance.handleClients();
        }
    }
    // stopping the server
    public static void stop() {stopped = true;}
}
