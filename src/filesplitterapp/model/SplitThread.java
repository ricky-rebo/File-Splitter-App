package filesplitterapp.model;

import filesplitterapp.model.splitter.CryptoSplitter;
import filesplitterapp.model.splitter.Splitter;
import filesplitterapp.model.splitter.SplitterException;
import filesplitterapp.model.splitter.ZipSplitter;

public class SplitThread extends Thread {
    private SplitFile splitFile;
    private Runnable callback;

    private boolean split = false;
    private Exception ex = null;

    public SplitThread(SplitFile splitFile, Runnable callback) {
        this.splitFile = splitFile;
        this.callback = callback;
    }

    @Override
    public void run() {
        //System.out.println("THREAD LAUNCHED");
        Splitter splitter;

        //System.out.println("> Calling split procedure ("+splitFile.filenameProperty().get()+")");
        try {
            switch(splitFile.getSplitMode()) {
                case DEFAULT:
                    splitter = new Splitter(splitFile.getSplitInfo());
                    break;
                case ZIP:
                    splitter = new ZipSplitter(splitFile.getSplitInfo());
                    break;
                case CRYPTO:
                    System.out.println("> Crypto Case");
                    splitter = new CryptoSplitter(splitFile.getSplitInfo(), splitFile.getCryptKey());
                    break;
                default:
                    return;
            }
            splitter.split();
            split = true;
        } catch (SplitterException ex) {
            this.ex = ex;
            split = false;
        }

        // Chiamata alla funzione di callback
        callback.run();
    }

    public SplitFile getSplitFile() {
        return splitFile;
    }

    public boolean isSplit() {
        return split;
    }

    public Exception getException() { return ex; }
}
