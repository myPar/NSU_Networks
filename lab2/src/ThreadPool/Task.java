package ThreadPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import ThreadPool.Data.TraverseStatus;
import ThreadPool.Data.DataTransferDescription;
import ThreadPool.Data.DataHeader;

public class Task implements Runnable {
// inner classes:
    // checks received file data or handle arisen exception while data transfer
    private class DataChecker {
        private ThreadPool.Data.TraverseStatus checkFileData(TraverseStatus inputStatus) {
            return null;
        }
    }
    // response to client about file checking status
    private class ClientResponder {
        private void responseToClient() {

        }
    }
    // closes connection with the client and remove received file data if some exception was arisen
    private class TaskCompleter {
        private void completeTaskExecution() {

        }
    }
// fields:
    // client socket
    private Socket clientSocket;
    // timeout of data transfer speed checking (ms)
    private final int checkSpeedTimeout = 10;
    // the output file name, data will save here
    private String outputFileName;
    // unique id of paired client
    private int clientId;
    // maximum buffer size
    private int maxBufferSize;
// constructor:
    public Task(Socket socket, String fileName, int clientId, int maxBufferSize) {
        clientSocket = socket;
        outputFileName = fileName;
        this.clientId = clientId;
        this.maxBufferSize = maxBufferSize;
    }
// complete task execution method
    private void completeTaskExecution(DataTransferDescription traverseDescription) {

    }
    @Override
    public void run() {
        assert clientSocket.isConnected();
        // client socket input stream
        InputStream socketInStream = null;
        // client socket output stream
        OutputStream socketOutStream = null;

        // default traverse status - success
        TraverseStatus status = TraverseStatus.SUCCESS_TRAVERSE;
        try {
            socketInStream = clientSocket.getInputStream();
            socketOutStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            status = TraverseStatus.SOCKET_EXCEPTION;
            String description = "Can't create socket I/O stream";
            completeTaskExecution(new DataTransferDescription(status, description));

            return;
        }
        // read data header: file name + expected file size:
        int maxHeaderSize = 1000;
        byte[] receiveDataBuffer = new byte[maxBufferSize];
        DataHeader header = null;
        int readByteCount;
        try {
            readByteCount = socketInStream.read(receiveDataBuffer);
        } catch (IOException e) {
            completeTaskExecution(new DataTransferDescription(TraverseStatus.SOCKET_EXCEPTION, "can't read data from socket"));
            return;
        }
        // create header object:
        try {
            header = new DataHeader(receiveDataBuffer, maxHeaderSize);
        } catch (DataHeader.HeaderException e) {
            // complete task execution in case of invalid header:
            switch(e.getType()) {
                case NAME: {
                    completeTaskExecution(new DataTransferDescription(TraverseStatus.INVALID_FILE_HEADER, "invalid file name"));
                    return;
                }
                case LENGTH: {
                    completeTaskExecution(new DataTransferDescription(TraverseStatus.INVALID_FILE_HEADER, "invalid file length"));
                    return;
                }
                case STRUCTURE: {
                    completeTaskExecution(new DataTransferDescription(TraverseStatus.INVALID_FILE_HEADER, "invalid header structure"));
                    return;
                }
                default:
                    assert false;
            }
        }
        // get header fields values:
        int expectedFileSize = header.getExpectedFileSize();
        int headerSize = header.getHeaderSize();
        String fileName = header.getFileName();

        completeTaskExecution(new DataTransferDescription(TraverseStatus.SUCCESS_TRAVERSE, ""));
    }
}
