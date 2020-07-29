package filesplitterapp.model;

import filesplitterapp.model.splitter.CryptoSplitter;
import filesplitterapp.model.splitter.Splitter;
import filesplitterapp.model.splitter.SplitterException;
import filesplitterapp.model.splitter.ZipSplitter;
import javafx.application.Platform;

/**
 * Thread che si occupa dello split di un singolo file.
 * Contiene al suo interno un oggetto {@code Splitinfo}, che si riferisce al file da dividere, e una funzione di callback,<br>
 * che il thread esegue subito prima di terminare.
 * <p>
 * Il thread contiene anche un flag 'split', che l'applicazione utilizza per capire se la procedura di split è andata a buon fine<br>
 * o meno e un riferimento ad un eccezione, in caso la procedura fallisca.
 *
 * @author Riccardo Rebottini
 */
public class SplitThread extends Thread {
    private SplitFile splitFile;
    private Runnable callback;

    private boolean split = false;
    private Exception ex = null;

    /** Crea un nuovo oggetto {@code SplitThread} con un file da dividere, e una funzione di callback */
    public SplitThread(SplitFile splitFile, Runnable callback) {
        this.splitFile = splitFile;
        this.callback = callback;
    }

    /**
     * Tenta di eseguire la procedura di split sul file passato nel costruttore.
     * Se la procedura va a buon fine viene impostato il flag 'split' a {@code true},<br>
     *     altrimenti viene salvata l'eccezione generata.
     * <p>
     * Prima di terminare, in ogni caso, viene eseguito il callback.
     */
    @Override
    public void run() {
        Splitter splitter;

        try {
            switch(splitFile.getSplitMode()) {
                case DEFAULT:
                    splitter = new Splitter(splitFile.getSplitInfo());
                    break;
                case ZIP:
                    splitter = new ZipSplitter(splitFile.getSplitInfo());
                    break;
                case CRYPTO:
                    splitter = new CryptoSplitter(splitFile.getSplitInfo(), splitFile.getCryptKey());
                    break;
                default:
                    return;
            }
            splitter.split();
            split = true;
        } catch (SplitterException ex) {
            this.ex = ex;
        }

        // Chiamata alla funzione di callback
        // Visto che i componenti "live" di JavaFX possono essere manipolati solo da thread di JavaFX stesso, viene rimandata
        // a Platform.runLater() la chiamata alla funzione di callback, che la esegue non appena questo thread finisce la sua esecuzione
        Platform.runLater(() -> callback.run());
    }


    /** Ritorna il file da dividere che è stato passato al thread */
    public SplitFile getSplitFile() {
        return splitFile;
    }

    /** Specifica se la procedura di split è andata a buon fine o meno */
    public boolean isSplit() {
        return split;
    }

    /** Ritorna l'eccezione generata in caso la procedura di split non sia andata a buon fine */
    public Exception getException() { return ex; }
}
