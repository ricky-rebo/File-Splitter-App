package filesplitterapp.model.splitter;

public class SecurableException extends Exception {

    public SecurableException(String msg) {
        super(msg);
    }

    public SecurableException(Throwable cause) {
        super(cause);
    }

    public SecurableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
