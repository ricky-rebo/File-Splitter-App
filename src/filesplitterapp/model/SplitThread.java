package filesplitterapp.model;

import filesplitterapp.model.splitter.CryptFileSplitter;
import filesplitterapp.model.splitter.FileSplitter;
import filesplitterapp.model.splitter.ZipFileSplitter;
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
        FileSplitter splitter = null;
        switch(splitFile.getSplitMode()) {
            case DEFAULT:
                splitter = new FileSplitter(splitFile.getSplitInfo());
                break;
            case ZIP:
                splitter = new ZipFileSplitter(splitFile.getSplitInfo());
                break;
            case CRYPTED:
                splitter = new CryptFileSplitter(splitFile.getSplitInfo(), splitFile.getCryptKey());
            default:
                return;
        }

        //TODO add custom exception on split() and remove a file from list only of it has been split correctly

        split = splitter.split(splitFile.getFinalDestPath());

        //I componenti di JavaFX possono essere modificati solo dal Thread di JavaFX, quindi
        // Usiamo Platform.runLater()
        callback.run();
    }

    public SplitFile getSplitFile() {
        return splitFile;
    }

    public boolean isSplit() {
        return split;
    }
}
