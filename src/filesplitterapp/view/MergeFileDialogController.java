package filesplitterapp.view;

import filesplitterapp.model.splitter.FileMerger;
import filesplitterapp.model.splitter.FileSplitterException;
import filesplitterapp.model.splitter.SplitInfo;
import filesplitterapp.model.splitter.SplitMode;
import filesplitterapp.util.Util;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.security.InvalidKeyException;

//TODO docs
public class MergeFileDialogController {
    private Stage dialogStage;
    private SplitInfo info = null;

    @FXML private Label lblPinfFile;
    @FXML private Label lblFile;
    @FXML private Label lblSplitMode;
    @FXML private Label lblParts;
    @FXML private Label lblKey;

    @FXML private TextField txtSaveTo;
    @FXML private TextField txtKey;

    @FXML private CheckBox chkDeleteFiles;

    /**
     * Default constructor
     */
    public MergeFileDialogController() {}


    /**
     * Initialize the controller class.
     * This method is automatically called after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        lblPinfFile.setText("");
        lblFile.setText("");
        lblSplitMode.setText("");
        lblParts.setText("");
        chkDeleteFiles.setSelected(false);
        showKeyField(false);
    }


    private void showKeyField(boolean show) {
        lblKey.setVisible(show);
        txtKey.setVisible(show);
    }


    /**
     * Sets the stage of this dialog
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }


    /**
     * Open a file chooser and let the user choose a .pinf file, and if a file
     * has been chosen it try to load Split Info and sets it
     *
     * @return true if a SplitInfoContainer object has been loaded correctly, false otherwise
     */
    public boolean chooseInfoFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Parts Info File ("+ SplitInfo.PINFO_EXT+")",
                "*"+ SplitInfo.PINFO_EXT));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showOpenDialog(stage);
        if(file == null) return false;

        SplitInfo tmp;
        try {
            tmp = SplitInfo.load(file);
            setInfo(file, tmp);
            return true;
        } catch (FileSplitterException ex) {
            Util.throwAlert(AlertType.ERROR, "File Splitter - ERROR", "File non caricato!", ex.getMessage());
            return false;
        }
    }


    private void setInfo(File infoFile, SplitInfo info) {
        this.info = info;

        lblPinfFile.setText(infoFile.getAbsolutePath());
        txtSaveTo.setText(infoFile.getParent());

        lblFile.setText(info.getName());
        lblSplitMode.setText(info.getSplitMode().toString());
        lblParts.setText((""+info.getParts()));

        if(info.getSplitMode() == SplitMode.CRYPTED)
            showKeyField(true);
    }


    // Button handlers
    @FXML
    private void handleChangeInfoFile() {
        chooseInfoFile(dialogStage);
    }


    @FXML
    private void handleChangeSaveTo() {
        String newDest = Util.chooseDirectory(txtSaveTo.getText(), dialogStage);
        if(newDest != null)  txtSaveTo.setText(newDest);
    }


    @FXML
    private void handleMerge() {
        FileMerger merger;
        try {
            merger = new FileMerger(info, txtKey.getText());
            merger.merge(txtSaveTo.getText());

            if(chkDeleteFiles.isSelected())
                merger.deletePartFiles();

            Util.throwAlert(AlertType.INFORMATION, "Merge File", "File ricomposto correttamente",
                    "Il file "+info.getName()+" è stato ricomposto correttamente.");
            dialogStage.close();
        } catch (InvalidKeyException e) {
            Util.throwAlert(AlertType.ERROR, "Merge file", "Errore!", e.getMessage());
        }
        //TODO add throws custom exception on merge() -> delete merged file if procedure fail
    }


    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
