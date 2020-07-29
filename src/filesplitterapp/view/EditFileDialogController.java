package filesplitterapp.view;

import filesplitterapp.MainApp;
import filesplitterapp.model.SplitFile;
import filesplitterapp.model.splitter.SplitInfo;
import filesplitterapp.model.splitter.SplitInfo.SplitMode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controller della view per modificare le impostazioni di divisione di un file.
 * <p>
 * Contiene i riferimenti a tutti i componenti della view con cui è necessario interagire, e definisce i comportamenti<br>
 * della view in risposta agli input dell'utente e dei click dei pulsanti
 * <p>
 * I riferimenti agli elementi e i gestori dei pulsanti sono contrassegnati con il tag @FXML, per poter essere visti dagli elementi<br>
 * all'interno del relativo file .fxml di questa view.
 *
 * @author Riccardo Rebottini
 */
public class EditFileDialogController {
	@FXML private Label lblFile;
	@FXML private Label lblValueType;
	@FXML private Label lblKey;

	@FXML private TextField txtValue;
	@FXML private TextField txtKey;
	@FXML private TextField txtDestPath;

	@FXML private ComboBox<SplitMode> comboSplitMode;
	@FXML private RadioButton rbPartNum;
	@FXML private RadioButton rbPartSize;
	@FXML private CheckBox chkApplyMultiple;
	@FXML private CheckBox chkSaveToSubDir;

	private MainApp mainApp;
	private Stage dialogStage;
	private SplitFile splitFile;
	private boolean okClicked = false;


	/**
	 * Costruttore base.<br>
	 * NOTA: L'inizializzazione vera e propria non avviene qui, ma nel metodo privato {@code initialize()}, che<br>
	 * viene chiamato automaticamente dopo che il file fxml è stato caricato.
	 */
	public EditFileDialogController() {}


	/*
	 * Inizializza il controller
	 * Questo metodo viene chiamato automaticamente dopo che il file .fxml è stato caricato
	 */
	@FXML
	private void initialize() {
		//Populate the combobox
		comboSplitMode.setItems(FXCollections.observableArrayList(SplitInfo.SplitMode.values()));

		//Aggiunge un listener alla combobox, se viene selezionato "CRYPTO" mostra i controlli relativi
		//alla chiave di cifratura
		comboSplitMode.valueProperty().addListener((observable, oldVal, newVal) -> showKeyFields(newVal == SplitMode.CRYPTO));

		//Aggiunge un listener ai radio buttons, in base a quello selezionato imposta il txtValue
		//su "Numero parti" o "Dimensione parti"
		rbPartNum.selectedProperty().addListener((observable, oldSel, newSel) -> {
			if(newSel) {
				lblValueType.setText("Numero parti");
				txtValue.setText(""+ splitFile.getPartsNum());
			}
		});
		rbPartSize.selectedProperty().addListener((observable, oldSel, newSel) -> {
			if(newSel) {
				lblValueType.setText("Dim. parti (Bytes)");
				txtValue.setText(""+ splitFile.getPartSize());
			}
		});

		//Di default disabilita la chechbox 'Applica a tutti'
		allowApplyToAll(false);
		chkApplyMultiple.selectedProperty().set(false);
	}


	/* Mostra o meno la label 'Chiave' e il relativo TextField */
	private void showKeyFields(boolean crypt) {
		lblKey.setVisible(crypt);
		txtKey.setVisible(crypt);
	}


	/** Imposta un file da modificare */
	public void setSplitFile(SplitFile splitFile) {
		this.splitFile = splitFile;

		lblFile.setText(splitFile.getAbsoluteFilename());
		comboSplitMode.getSelectionModel().select(splitFile.getSplitMode());

		txtDestPath.setText(splitFile.getDestPath());
		chkSaveToSubDir.setSelected(splitFile.useSubDir());

		rbPartSize.setSelected(true);
		if(splitFile.getCryptKey() != null) txtKey.setText(splitFile.getCryptKey());
	}


	/** Imposta lo stage di questa view */
	public void setDialogStage(Stage stage) { dialogStage = stage; }


	/**
	 * Imposta un riferimento all'applicazione principale.
	 * <p>
	 * Questo metodo viene utilizzato dall'applicazione principale per restituire un riferimento a se stessa
	 */
	public void setMainApp(MainApp mainApp) { this.mainApp = mainApp; }


	/** Visualizza la checkbox 'Applica a tutti', utilizzata in caso di inserimento di più file alla volta */
	public void allowApplyToAll(boolean b) { chkApplyMultiple.setVisible(b); }

	/**
	 * Ritorna {@code true} se l'utente ha selezionato la checkbox 'Applica a tutti'.
	 * <p>
	 * NOTA: richiede che la checkbox sia abilitata, tramite {@code allowApplyToAll(true}}
	 */
	public boolean applyToAllSelected() { return chkApplyMultiple.selectedProperty().get(); }


	/** Ritorna {@code true} se l'utente ha cliccato 'OK', altrimenti false */
	public boolean isOkClicked() {
		return okClicked;
	}


	/*
	Modifica il percorso in cui salvare le parti del file diviso.
	Sceglie una nuova directory del file system tramite il metodo chooseDirectory() di MainApp
	 */
	@FXML
	private void handleChangeDestDir() {
		String newDest = mainApp.chooseDirectory(txtDestPath.getText(), dialogStage);
		if(newDest != null) txtDestPath.setText(newDest);
	}


	/*
	Verifica che l'input inserito dall'utente sia valido.
	In caso positivo salva all'interno dell'oggetto SplitFile le nuove impostazioni, e chiude il dialog.
	In caso contrario lancia un alert per avvisare l'utente che l'input non è valido.
	 */
	@FXML
	private void handleOK() {
		if(isInputValid()) {
			splitFile.setSplitMode(comboSplitMode.getSelectionModel().getSelectedItem());
			if(rbPartNum.isSelected())
				splitFile.setPartsNum(Integer.parseInt(txtValue.getText()));
			else
				splitFile.setPartSize(Integer.parseInt(txtValue.getText()));
			splitFile.setDestPath(txtDestPath.getText(), chkSaveToSubDir.isSelected());
			if(txtKey.isVisible())
				splitFile.setCryptKey(txtKey.getText());

			okClicked = true;
			dialogStage.close();
		}
	}


	/* Chiude il dialog senza salvare eventuali cambiamenti. */
	@FXML
	private void handleCancel() {
		dialogStage.close();
	}


	/* Verifica che l'input dell'utente sia valido. in caso positivo ritorna true, altrimenti false. */
	private boolean isInputValid() {
		String err = "";

		if(txtValue.getText() == null || txtValue.getText().length() == 0)
			err += "Campo "+lblValueType.getText()+" non valido!\n";
		else {
			try { Integer.parseInt(txtValue.getText()); }
			catch(Exception ex) { err += lblValueType.getText()+" deve essere un numero!"; }
		}

		if(txtDestPath.getText()==null || txtDestPath.getText().length()==0 || !new File(txtDestPath.getText()).isDirectory())
			err += "Percorso di salvataggio non valido!\n";


		if(txtKey.isVisible())
			if(txtKey.getText()==null || txtKey.getText().length()==0 || txtKey.getText().length() > 32)
				err += "Campo chiave non valido!";


		// Check if there are errors
		if(err.length() == 0)
			return true;
		else {
			mainApp.throwAlert(AlertType.ERROR, "FileSplitter", "Errore\nInput non valido!", err);
			return false;
		}
	}
}
