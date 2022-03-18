package Core;


import Attachments.BaseAttachment;
import Exceptions.HandlerException;
import Exceptions.ServerException;
import Handlers.HandlerFactory;
import Logger.ExceptionLogger;
import Logger.GlobalLogger;
import Attachments.BaseAttachment.KeyState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
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
            ExceptionLogger.logException(e, exceptionLogger);
        }
    }
    private void initDatagramChannel() throws ServerException {
        try {
            DatagramChannel udpChannel = DatagramChannel.open();
            udpChannel.configureBlocking(false);
            udpChannel.register(selector, SelectionKey.OP_READ, new BaseAttachment(KeyState.DNS_RESPONSE));
        }
        catch (IOException e) {
            ExceptionLogger.logException(e, exceptionLogger);
        }
    }

    // main handle loop
    private void handleClients() {
        while(!Server.stopped) {
            try {
                if(selector.select() > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    SelectionKey curKey;
                    while (keyIterator.hasNext()) {
                        curKey = keyIterator.next();
                        // remove key from set to prevent handling it twice (selector doesn't remove key by itself)
                        keyIterator.remove();
                        handle(curKey);
                    }
                }
            }
            catch(Exception e) {
                ExceptionLogger.logException(e, exceptionLogger);
            }
        }
        closeChannels();
        // update flags
        Server.stopped = false;
        Server.started = false;
    }
    // handle single key method
    private void handle(SelectionKey key) throws Exception {
        Handler keyHandler = HandlerFactory.getHandler(key);
        keyHandler.handle(key);
    }
    // close all channels (server interrupt case)
    private void closeChannels() {
        Set<SelectionKey> keys = selector.keys();
        // close all registered channels:
        for (SelectionKey key: keys) {
            SelectableChannel channel = key.channel();
            try {
                channel.close();
            }
            catch (IOException e) {
                ExceptionLogger.logException(e, exceptionLogger);   // TODO log Server exception instead of I/O exception
            }
        }
        // close the selector:
        try {
            selector.close();
        }
        catch (IOException e) {
            ExceptionLogger.logException(e, exceptionLogger);
        }
    }

    // main server start method
    public static void start(int port, GlobalLogger.Mode mode) {
        if (!Server.started) {
            Server.started = true;
            Server instance = null;

            // create server instance:
            try {
                instance = new Server(port, mode);
            } catch (Exception e) {
                ExceptionLogger.logException(e, exceptionLogger);
                System.exit(1);
            }
            // handle clients:
            try {
                instance.handleClients();
            }
            catch (Exception e) {
                ExceptionLogger.logException(e, exceptionLogger);
                instance.closeChannels();
                System.exit(1);
            }
        }
    }
    // stopping the server
    public static void stop() {stopped = true;}
}
