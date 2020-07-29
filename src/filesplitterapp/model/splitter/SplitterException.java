package filesplitterapp.model.splitter;

public class SplitterException extends Exception {

    public SplitterException(String msg) {
        super(msg);
    }

    public SplitterException(Throwable cause) {
        super(cause);
    }

    public SplitterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
