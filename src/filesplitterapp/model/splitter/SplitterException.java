package filesplitterapp.model.splitter;

public class SplitterException extends Exception {
    private String message;
    private Exception exception;

    public SplitterException(String message, Exception exception) {
        this.message = message;
        this.exception = exception;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }
}
