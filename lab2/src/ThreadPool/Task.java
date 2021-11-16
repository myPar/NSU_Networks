package ThreadPool;

import java.net.Socket;

public class Task implements Runnable {
// exception class of data transfer
    public static class DataTransferException extends Exception {
        private TraverseStatus exceptionType;
        private String description;
        // constructor:
        public DataTransferException(TraverseStatus type, String desc) {
            exceptionType = type;
            description = desc;
        }
        // print exception method
        public void printException() {

        }
        // get exception message method
        public String getExceptionMessage() {
            return "Data transfer exception of type: " + exceptionType.getValue() + ": " + description;
        }
    }
// enum represents data transfer status
    private enum TraverseStatus {
        // TODO add other enum values
        INVALID_FILE_DATA("INVALID FILE DATA");
        private String value;

        TraverseStatus(String value) {
            this.value = value;
        }
        // get enum String value method
        String getValue() {
            return value;
        }
    }
// inner classes:
    // checks received file data or handle arisen exception while data transfer
    private class DataChecker {
        private TraverseStatus checkFileData(TraverseStatus inputStatus) {
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
    // the output file name, data will br save here
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
    @Override
    public void run() {

    }
}
