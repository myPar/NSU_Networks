package Core;

import UI.GUI;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Client {
    // mode of client executing: TEST - file header can be customize, NORMAL - file header constructs automatically
    public enum Mode {TEST, NORMAL}
// client exception class
    public static class ClientException extends Exception {
        private ClientExceptionType exceptionType;
        private String description;

        public ClientException(ClientExceptionType type, String dscr) {
            exceptionType = type;
            description = dscr;
        }
        // get exception message method
        String getExceptionMessage() {
            return "Server exception of type: " + exceptionType.getValue() + ": " + description;
        }
    }
// client exception type enum
    public enum ClientExceptionType {
        CLOSE_CONNECTION_EXCEPTION("CLOSE CONNECTION EXCEPTION"),
        CONFIGURATION_EXCEPTION("CONFIGURATION EXCEPTION"),
        FILE_SENDER_EXCEPTION("FILE SENDER EXCEPTION"),
        SERVER_RESPONSE_HANDLER_EXCEPTION("SERVER RESPONSE HANDLER EXCEPTION");
        private String value;

        ClientExceptionType(String value) {
            this.value = value;
        }
        // get enum String value method
        String getValue() {
            return value;
        }
    }
// inner classes:
    // sends file to server
    private class FileSender extends Thread {
        private void execute() throws ClientException {
            FileInputStream input;
            try {
                input = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new ClientException(ClientExceptionType.FILE_SENDER_EXCEPTION, "can't open the file " + filePath + " for reading");
            }
            // get header
            byte[] buffer = new byte[outputBufferSize];
            HeaderGetter headerGetter = new HeaderGetter();
            byte[] header = headerGetter.getHeader(mode, new File(filePath));

            // read byte count
            int count;

            // read data from file and send it to server
            try {
                // send header
                socketOutputStream.write(header);
                // send file data
                while ((count = input.read(buffer)) > 0 || !Thread.currentThread().isInterrupted()) {
                    socketOutputStream.write(buffer, 0, count);
                }
            }
            catch (IOException e) {
                throw new ClientException(ClientExceptionType.FILE_SENDER_EXCEPTION, "I/O exception - can't read/write data from/to socket");
            }
        }
        @Override
        public void run() {
            try {
                execute();
            }
            catch(ClientException e) {

            }
        }
    }
    // handle server response
    private class ServerResponseHandler extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[inputBufferSize];
            try {
                int count = socketInputStream.read(buffer);
                // get server message
                String serverMessage = new String(Arrays.copyOf(buffer, count));
                // TODO: give output to gui
            }
            catch (IOException e) {
                completeExecution(new ClientException(ClientExceptionType.SERVER_RESPONSE_HANDLER_EXCEPTION, "exception while reading server response"));
            }
        }
    }
    // client socket
    private Socket clientSocket;
    // socket I/O streams:
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    // path to file which will be sent
    private String filePath;
    // client executing mode
    private Mode mode;
    // file sender buffer size
    private final int outputBufferSize = 16000;
    // server responder buffer size
    private final int inputBufferSize = 1000;
    // Client parts:
    FileSender sender;
    ServerResponseHandler responseHandler;
    GUI gui;
// constructor:
    public Client(Mode mode, String filePath, String serverAddress, int serverPort, GUI gui) {
        this.mode = mode;
        this.gui = gui;
        InetAddress serverInetAddress;
        try {
            serverInetAddress = config(filePath, serverAddress);
        } catch (ClientException e) {
            gui.displayClientException(e);
            return;
        }
        try {
            clientSocket = new Socket(serverInetAddress, serverPort);
            // get I/O streams of client socket
            socketInputStream = clientSocket.getInputStream();
            socketOutputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.filePath = filePath;
        // init Client parts:
        sender = new FileSender();
        responseHandler = new ServerResponseHandler();
    }
// config client method (returns server InetAddress)
    private InetAddress config(String filePath, String serverAddress) throws ClientException{
        File file = new File(filePath);

        // check does input file exists
        if (!file.exists()) {
            throw new ClientException(ClientExceptionType.CONFIGURATION_EXCEPTION, "file with name " + filePath + " doesn't exists");
        }
        // try get server address from string
        InetAddress resultAddress;
        try {
            resultAddress = InetAddress.getByName(serverAddress);
        } catch (UnknownHostException e) {
            throw new ClientException(ClientExceptionType.CONFIGURATION_EXCEPTION, "invalid server address: " + serverAddress);
        }
        return resultAddress;
    }
    // mark client Thread parts as interrupt
    private void interrupt() {
        responseHandler.interrupt();
        sender.interrupt();
    }
    // complete client execution method (if no exceptions on client side exception equals null)
    public void completeExecution(ClientException exception) {
        if (exception != null) {
            gui.displayClientException(exception);
            try {
                clientSocket.close();
            } catch (IOException e) {
                gui.displayClientException(new ClientException(ClientExceptionType.CLOSE_CONNECTION_EXCEPTION, "can't close socket"));
            }
            responseHandler.interrupt();
            sender.interrupt();
        }
    }
    // main execute method
    public void execute() {

    }
}
