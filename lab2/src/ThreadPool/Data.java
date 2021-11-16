package ThreadPool;

import java.util.Arrays;

// class represents auxiliary data class: classes, enums which are used in class Task
class Data {
    // exception class of data transfer
    static class DataTransferDescription {
        private TraverseStatus status;
        private String description;
        // constructor:
        DataTransferDescription(TraverseStatus status, String desc) {
            this.status = status;
            description = desc;
        }
        // get exception message method
        String getDescriptionMessage() {
            if (status == TraverseStatus.SUCCESS_TRAVERSE) {
                return status.getValue();
            }
            return "Data transfer exception of type: " + status.getValue() + ": " + description;
        }
    }
    // enum represents data transfer status
    enum TraverseStatus {
        SUCCESS_TRAVERSE("SUCCESS TRAVERSE"),
        INVALID_FILE_DATA("INVALID FILE DATA"),
        INVALID_FILE_HEADER("INVALID FILE HEADER"),
        SOCKET_EXCEPTION("SOCKET EXCEPTION");
        private String value;

        TraverseStatus(String value) {
            this.value = value;
        }
        // get enum String value method
        String getValue() {
            return value;
        }
    }
    static class DataHeader {
        // empty exception class - just to throw exception
        static class HeaderException extends Exception {
            enum Type{NAME, LENGTH, STRUCTURE}
            private Type type;

            HeaderException(Type t) {type = t;}
            Type getType() {return type;}
        }
        // fields:
        private int headerSize;
        private String fileName;
        private int expectedFileSize;
        private final String delimiter = "\\\\";
        private final int minTokenCount = 4;

        // field getters:
        int getExpectedFileSize() {return expectedFileSize;}
        String getFileName() {return fileName;}
        int getHeaderSize() {return headerSize;}

        // constructor
        DataHeader(byte[] data, int maxHeaderSize) throws HeaderException {
            // get file name (1. convert necessary bytes to String 2. split by delimiter):
            String byteString = new String(Arrays.copyOf(data, maxHeaderSize));
            String[] strArr = byteString.split(delimiter);

            // check header structure (expected: name + delimiter + size + delimiter + data):
            if (strArr.length < minTokenCount) {
                throw new HeaderException(HeaderException.Type.STRUCTURE);
            }
            fileName = strArr[0];

            // check is file name valid:
            if (fileName.equals("")) {
                throw new HeaderException(HeaderException.Type.NAME);
            }
            // read expected file size:
            String intStr = strArr[2];
            int size;
            try {
                size = Integer.parseInt(intStr);
            }
            catch (NumberFormatException e) {
                throw new HeaderException(HeaderException.Type.LENGTH);
            }
            headerSize = fileName.length() + delimiter.length() * 2 + intStr.length();
            expectedFileSize = size;
        }
    }
}
