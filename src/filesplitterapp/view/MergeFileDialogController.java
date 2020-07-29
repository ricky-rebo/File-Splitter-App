package filesplitterapp.view;

import filesplitterapp.MainApp;
import filesplitterapp.model.splitter.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.security.InvalidKeyException;

/**
 * Controller della view per unire un file diviso.
 * <p>
 * Contiene i riferimenti a tutti i componenti della view con cui è necessario interagire, e definisce i comportamenti<br>
 * della view in risposta agli input dell'utente e dei click dei pulsanti
 * <p>
 * I riferimenti agli elementi e i gestori dei pulsanti sono contrassegnati con il tag @FXML, per poter essere visti dagli elementi<br>
 * all'interno del relativo file .fxml di questa view.
 *
 * @author Riccardo Rebottini
 */
public class MergeFileDialogController {
    @FXML private Label lblPinfFile;
    @FXML private Label lblFile;
    @FXML private Label lblSplitMode;
    @FXML private Label lblParts;
    @FXML private Label lblKey;

    @FXML private TextField txtSaveTo;
    @FXML private TextField txtKey;

    @FXML private CheckBox chkDeleteFiles;

    private MainApp mainApp;
    private Stage dialogStage;
    private SplitInfo info = null;

    /**
     * Costruttore base.
     * <p>
     * NOTA: L'inizializzazione vera e propria non avviene qui, ma nel metodo privato {@code initialize()}, che<br>
     * viene chiamato automaticamente dopo che il file fxml è stato caricato.
     */
    public MergeFileDialogController() {}


    /*
     * Inizializza il controller
     * Questo metodo viene chiamato automaticamente dopo che il file .fxml è stato caricato
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

    /* Mostra il campo per inserire la chiave, e la relativa label */
    private void showKeyField(boolean show) {
        lblKey.setVisible(show);
        txtKey.setVisible(show);
    }


    /** Imposta lo stage di questo dialog */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }


    /** Imposta un riferimento all'applicazione principale.
     * <p>
     * Viene utilizzato dall'applicazione principale per restituire un riferimento a se stessa.
     */
    public void setMainApp(MainApp mainApp) { this.mainApp = mainApp; }


    /**
     * Permette all'utente di scegliere un file {@value filesplitterapp.model.splitter.SplitInfo#PINFO_EXT} tramite un {@code FileChooser}.
     * <p>
     * Se l'utente non seleziona nessun file ritorna semplicemente false.<br>
     * Se invece l'utente seleziona un file {@value filesplitterapp.model.splitter.SplitInfo#PINFO_EXT} che non viene caricato correttamente viene<br>
     * mostrato un alert per segnalare l'errore all'utente.
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
        } catch (SplitterException ex) {
            mainApp.throwAlert(AlertType.ERROR, "File Splitter - ERROR", "File non caricato!", ex.getMessage());
            return false;
        }
    }


    /**
     * Imposta un file da unire all'interno del dialog.
     * @param infoFile il file {@value filesplitterapp.model.splitter.SplitInfo#PINFO_EXT} selezionato
     * @param info l'oggetto {@code SplitInfoContainer} caricato dal file {@value filesplitterapp.model.splitter.SplitInfo#PINFO_EXT}
     */
    private void setInfo(File infoFile, SplitInfo info) {
        this.info = info;

        lblPinfFile.setText(infoFile.getAbsolutePath());
        txtSaveTo.setText(infoFile.getParent());

        lblFile.setText(info.getName());
        lblSplitMode.setText(info.getSplitMode().toString());
        lblParts.setText((""+info.getPartsNum()));

        showKeyField(info.getSplitMode() == SplitInfo.SplitMode.CRYPTO);
    }


    // Button handlers
    @FXML
    private void handleChangeInfoFile() {
        chooseInfoFile(dialogStage);
    }


    @FXML
    private void handleChangeSaveTo() {
        String newDest = mainApp.chooseDirectory(txtSaveTo.getText(), dialogStage);
        if(newDest != null)  txtSaveTo.setText(newDest);
    }


    @FXML
    private void handleMerge() {
        //Se la destinazione del file è stata cambiata nel form, viene cambiata anche nel'oggetto info
        if(!info.getFile().getParent().equals(txtSaveTo.getText())) info.setFileLocation(txtSaveTo.getText());

        if(mainApp.mergeFile(info, txtKey.getText(), chkDeleteFiles.isSelected()))
            dialogStage.close();
    }


    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
