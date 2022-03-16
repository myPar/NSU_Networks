package Exceptions;

public class BaseException extends Exception {
    protected String exceptionClass;
    protected String exceptionType;
    protected String description;

    protected final String getExceptionClass() {return exceptionClass;}
    protected final String getExceptionType() {return exceptionType;}
    protected final String getExceptionDescription() {return description;}

    public String getBaseMessage() {
        return exceptionClass + " exception: " + exceptionType + " - " + description;
    }
}
