package Exceptions;

public class BaseException extends Exception {
    protected String exceptionClass;
    protected String exceptionType;
    protected String description;

    String getExceptionData() {
        return "";
    }
}
