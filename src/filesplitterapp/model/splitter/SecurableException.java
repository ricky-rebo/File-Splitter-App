package filesplitterapp.model.splitter;


/**
 * {@code SecurableException} è un tipo di eccezione che viene lanciata dalle funzioni dell'interfaccia {@code Securable.}
 *
 * @author Riccardo Rebottini
 */
public class SecurableException extends Exception {

    /*public SecurableException(String msg) {
        super(msg);
    }

    public SecurableException(Throwable cause) {
        super(cause);
    }*/

    /** Crea una nuova eccezione {@code SecurableException} specificando un messaggio e una causa */
    public SecurableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
