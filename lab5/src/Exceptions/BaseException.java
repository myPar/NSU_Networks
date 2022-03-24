package Exceptions;

public class BaseException extends Exception {
    private String exceptionClass;
    private String exceptionType;
    private String description;

    protected final String getExceptionClass() {return exceptionClass;}
    protected final String getExceptionType() {return exceptionType;}
    protected final String getExceptionDescription() {return description;}

    public String getBaseMessage() {
        return exceptionClass + " exception: " + exceptionType + " - " + description;
    }

    BaseException(String cls, String dscr, String type) {
        this.description = dscr;
        this.exceptionClass = cls;
        this.exceptionType = type;
    }
}
