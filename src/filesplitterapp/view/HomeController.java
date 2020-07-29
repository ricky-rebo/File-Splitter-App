package filesplitterapp.view;

import filesplitterapp.MainApp;
import filesplitterapp.model.SplitFile;
import filesplitterapp.model.SplitThread;
import filesplitterapp.model.splitter.SplitMode;
import filesplitterapp.util.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

//TODO docs
public class HomeController {
	//FXML Elements
	@FXML private TableView<SplitFile> fileTable;
	@FXML private TableColumn<SplitFile, String> fileCol;
	@FXML private TableColumn<SplitFile, SplitMode> modeCol;
	@FXML private TableColumn<SplitFile, Number> sizeCol;
	@FXML private TableColumn<SplitFile, Number> nPartsCol;
	@FXML private TableColumn<SplitFile, String> destCol;

	@FXML private Label lblProgress;
	@FXML private ProgressBar progressbar;

	@FXML private SplitPane splitPane;

	//Reference to the main application
	private MainApp mainApp;

	//Progressbar management
	private int totalSteps = 0;
	private boolean completed;


	/**
	 * The constructor, called before the initialize() method.
	 */
	public HomeController() {}


	/**
	 * Initialize the controller class.
	 * This method is automatically called after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		//Fix the divider position
		splitPane.getDividers().get(0).positionProperty().addListener(
				(observable, oldValue, newValue) -> splitPane.getDividers().get(0).setPosition(0.7));

		//Remove text from the progress label
		lblProgress.setText("");


		//Set the table columns data factories
		fileCol.setCellValueFactory(
				cellData -> cellData.getValue().filenameProperty());
		modeCol.setCellValueFactory(
				cellData -> cellData.getValue().splitModeProperty());
		sizeCol.setCellValueFactory(
				cellData -> cellData.getValue().partSizeProperty());
		nPartsCol.setCellValueFactory(
				cellData -> cellData.getValue().partsNumProperty());
		destCol.setCellValueFactory(
				cellData -> cellData.getValue().destPathProperty());

		//Edit the selected file if double click on a row
		fileTable.setRowFactory(tv -> {
			TableRow<SplitFile> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					mainApp.showEditFileDialog(fileTable.getSelectionModel().getSelectedItem(), false);
				}
			});
			return row;
		});
	}


	/**
	 * Is called by the main application to give a reference back to itself.
	 */
	public void setMainApp(MainApp app) {
		this.mainApp = app;
		fileTable.setItems(mainApp.getFileList());
	}


	//TODO docs
	private void resetProgress(int steps) {
		lblProgress.setText("");
		progressbar.setProgress(0);
		totalSteps = steps;
		completed = false;
	}

	//TODO docs
	public synchronized void incProgress() {
		if(completed || totalSteps==0) return;

		progressbar.setProgress(progressbar.getProgress() + 1.0/(double)totalSteps);
		lblProgress.setText("Completamento... " + progressbar.getProgress()*100 + "%");
		if(progressbar.getProgress() == 1)
			lblProgress.setText("Completato");
	}


	@FXML
	private void handleRemoveAll() {
		fileTable.getItems().clear();
	}


	@FXML
	private void handleRemove() {
		int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
		if(selectedIndex >= 0)
			fileTable.getItems().remove(selectedIndex);
		else
			Util.throwAlert(AlertType.WARNING, "Filesplitter", "Nessun file selezionato!",
					"Per rimuovere un file dalla lista devi prima selezionarlo!");
	}


	@FXML
	private void handleEdit() {
		int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
		if(selectedIndex >= 0) {
			mainApp.showEditFileDialog(fileTable.getSelectionModel().getSelectedItem(), false);
		}
		else
			Util.throwAlert(AlertType.WARNING, "Filesplitter", "Nessun file selezionato!",
					"Per modificare un file devi prima selezionarlo!");
	}


	@FXML
	private void handleAdd() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		List<File> files = fileChooser.showOpenMultipleDialog(mainApp.getPrimaryStage());

		//Se è stato selezionato più di un file viene permesso di applicare le opzioni a più file insieme
		boolean multipleFiles = files.size() > 1;
		boolean useModel = false;
		SplitFile model = null;
		for(File f: files) {
			if(useModel) { mainApp.getFileList().add(model.getCopy(f)); }
			else {
				SplitFile tmp = new SplitFile(f, SplitMode.DEFAULT);
				boolean[] flags = mainApp.showEditFileDialog(tmp, multipleFiles);
				if(flags[0]) {
					mainApp.getFileList().add(tmp);
					if(flags[1]) {
						model = tmp;
						useModel = true;
					}
				}
			}
		}
	}


	@FXML
	private void handleSplitFiles() {
		int listDim = mainApp.getFileList().size();
		SplitThread threads[] = new SplitThread[listDim];
		String failed = "";
		resetProgress(listDim);

		//Creazione threads
		for(int i=0; i<listDim; i++) {
			threads[i] = new SplitThread(mainApp.getFileList().get(i), () -> Platform.runLater(() -> this.incProgress()));
			threads[i].start();
		}

		//Join per attendere che tutti abbiano finito
		for(int i=0; i<listDim; i++) {
			try { threads[i].join(); }
			catch (InterruptedException ex) {
				//TODO add error handling
				ex.printStackTrace();
			}
		}

		//Rimozione file completati o eventuale segnalazione errori
		for(int i=0; i<listDim; i++) {
			if(threads[i].isSplit())
				mainApp.getFileList().remove(threads[i].getSplitFile());
			else
				failed += "- "+threads[i].getSplitFile().filenameProperty().get()+"\n";
		}

		if(failed.length() == 0)
			Util.throwAlert(AlertType.INFORMATION, "Split Files", "Operazione completata",
				"Tutti i file sono stati divisi con successo.");
		else
			Util.throwAlert(AlertType.WARNING, "Split Files", "Attenzione",
				"I seguenti file non sono stati divisi:\n\n"+failed);
	}


	@FXML
	private void handleMergeFiles() {
		mainApp.showMergeFileDialog();
	}
}
