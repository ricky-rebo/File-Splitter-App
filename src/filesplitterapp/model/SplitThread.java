package filesplitterapp.model;

import filesplitterapp.model.splitter.CryptoSplitter;
import filesplitterapp.model.splitter.Splitter;
import filesplitterapp.model.splitter.ZipSplitter;
import filesplitterapp.view.HomeController;

public class SplitThread extends Thread {
    private SplitFile splitFile;
    private HomeController controller;
    private Runnable callback;

    private boolean split= false;

    public SplitThread(SplitFile splitFile, Runnable callback) {
        this.splitFile = splitFile;
        this.callback = callback;
    }

    @Override
    public void run() {
        //System.out.println("THREAD LAUNCHED");
        Splitter splitter = null;
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

        if(splitter == null) { System.out.println("> Error, "+splitter.getClass()+" is null!"); return; }

        //TODO add custom exception on split() and remove a file from list only of it has been split correctly

        System.out.println("> Calling split procedure ("+splitFile.filenameProperty().get()+")");
        split = splitter.split();

        //I componenti di JavaFX possono essere modificati solo dal Thread di JavaFX, quindi
        // si utilizza Platform.runLater() che esegue una data procedura quando il thread termina
        // la sua esecuzione
        callback.run();
    }

    public SplitFile getSplitFile() {
        return splitFile;
    }

    public boolean isSplit() {
        return split;
    }
}
