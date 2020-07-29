package filesplitterapp.model;

import filesplitterapp.model.splitter.*;
import filesplitterapp.view.HomeController;

public class SplitThread extends Thread {
    private SplitFile splitFile;
    private Runnable callback;

    private boolean split= false;

    public SplitThread(SplitFile splitFile, Runnable callback) {
        this.splitFile = splitFile;
        this.callback = callback;
    }

    @Override
    public void run() {
        //System.out.println("THREAD LAUNCHED");
        Splitter splitter;
        //TODO add custom exception on split() and remove a file from list only of it has been split correctly

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
            split = splitter.split();
        } catch (SplitterException | SecurableException e) {
            e.printStackTrace(); //TODO add proper error handling
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
}
