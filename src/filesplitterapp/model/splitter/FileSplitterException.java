package filesplitterapp.model.splitter;

public class FileSplitterException extends Exception {
    private String message;
    private Exception exception;

    public FileSplitterException(String message, Exception exception) {
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
