package filesplitterapp.model.splitter;


/**
 * {@code SplitterException} è un tipo di eccezione lanciata dalle funzioni da {@code Splitter} e {@code Merger} (e relative classi derivate).
 *
 * @author Riccardo Rebottini
 */
public class SplitterException extends Exception {

    /** Crea una nuova {@code SplitterException} specificando un messaggio */
    public SplitterException(String msg) {
        super(msg);
    }

    /** Crea una nuova {@code SplitterException} specificando una una causa */
    public SplitterException(Throwable cause) {
        super(cause);
    }

    /** Crea una nuova {@code SplitterException} specificando un messaggio e una causa */
    public SplitterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
