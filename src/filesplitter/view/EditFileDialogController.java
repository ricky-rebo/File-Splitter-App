package filesplitter.view;

import filesplitter.model.SplitFile;
import filesplitter.model.splitter.SplitMode;
import filesplitter.util.Util;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;

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

	private Stage dialogStage;
	private SplitFile splitFile;
	private boolean okClicked = false;

	/**
	 * Initialize the controller class.
	 * This method is automatically called right after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		//Populate the combobox
		comboSplitMode.setItems(FXCollections.observableArrayList(SplitMode.values()));

		//Add a listener on the combo selected value
		//If CRYPTED mode selected show the key field
		comboSplitMode.valueProperty().addListener((observable, oldVal, newVal) -> {
			boolean crypt = (newVal == SplitMode.CRYPTED) ? true : false;
			lblKey.setVisible(crypt);
			txtKey.setVisible(crypt);
		});

		//Add listeners on radiobutton selection
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

		//Set default multiple apply false
		allowMultipleApply(false);
		chkApplyMultiple.selectedProperty().set(false);
	}


	/**
	 * Sets a SplitInfo object to be edited.
	 */
	public void setSplitFile(SplitFile splitFile) {
		this.splitFile = splitFile;

		lblFile.setText(splitFile.getAbsoluteFilename());
		comboSplitMode.getSelectionModel().select(splitFile.getSplitMode());

		txtDestPath.setText(splitFile.getDestPath());
		chkSaveToSubDir.setSelected(splitFile.saveToSubDir());

		rbPartSize.setSelected(true);
		if(splitFile.getCryptKey() != null) txtKey.setText(splitFile.getCryptKey());
	}


	/**
	 * Sets the Stage of the dialog
	 */
	public void setDialogStage(Stage stage) { dialogStage = stage; }


	/**
	 * Toggle the possibility to apply settings to multiple files
	 * @param b true to allow multiple apply, false otherwise
	 */
	public void allowMultipleApply(boolean b) { chkApplyMultiple.setVisible(b); }

	/**
	 * Returns true if the user has checked the apply multiple checkbox, false otherwise
	 * NOTE: It requires to set AllowMultipleApply(true)!
	 */
	public boolean applyMultiple() { return chkApplyMultiple.selectedProperty().get(); }


	/**
	 * Returns true if the user clicked OK on the dialog, false otherwise
	 */
	public boolean isOkClicked() {
		return okClicked;
	}


	@FXML
	private void handleChangeDestDir() {
		String newDest = Util.chooseDirectory(txtDestPath.getText(), dialogStage);
		if(newDest != null) txtDestPath.setText(newDest);
	}


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


	@FXML
	private void handleCancel() {
		dialogStage.close();
	}


	/**
	 * Validate the inserted data in the dialog before set it to the splitFile object
	 * @return true if the input is valid, false otherwise
	 */
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
			Util.throwAlert(AlertType.ERROR, "FileSplitter", "Errore\nInput non valido!", err);
			return false;
		}
	}
}
