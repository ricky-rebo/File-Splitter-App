package filesplitter.model;

import filesplitter.model.splitter.CryptFileSplitter;
import filesplitter.model.splitter.FileSplitter;
import filesplitter.model.splitter.ZipFileSplitter;
import filesplitter.view.HomeController;
import javafx.application.Platform;

public class SplitThread extends Thread {
    private SplitFile splitFile;
    private HomeController controller;

    private boolean splitted = false;

    public SplitThread(SplitFile splitFile, HomeController controller) {
        this.splitFile = splitFile;
        this.controller = controller;
    }

    @Override
    public void run() {
        System.out.println("THREAD LAUNCHED");
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

        splitted = splitter.split(splitFile.getFinalDestPath());

        //I componenti di JavaFX possono essere modificati solo dal Thread di JavaFX, quindi
        // Usiamo Platform.runLater()
        Platform.runLater(() -> controller.incProgress());
    }

    public SplitFile getSplitFile() {
        return splitFile;
    }

    public boolean isSplitted() {
        return splitted;
    }
}
