package filesplitterapp.view;

import filesplitterapp.MainApp;
import filesplitterapp.model.SplitFile;
import filesplitterapp.model.SplitThread;
import filesplitterapp.model.splitter.SplitInfo;
import filesplitterapp.model.splitter.SplitInfo.SplitMode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Controller della view principale dell'applicazione.
 * <p>
 * Contiene i riferimenti a tutti i componenti della view con cui è necessario interagire, e definisce i comportamenti<br>
 * della view in risposta agli input dell'utente e dei click dei pulsanti
 * <p>
 * I riferimenti agli elementi e i gestori dei pulsanti sono contrassegnati con il tag @FXML, per poter essere visti dagli elementi<br>
 * all'interno del relativo file .fxml di questa view.
 *
 * @author Riccardo Rebottini
 */
public class HomeController {
	//FXML Elements
	@FXML private TableView<SplitFile> fileTable;
	@FXML private TableColumn<SplitFile, String> fileCol;
	@FXML private TableColumn<SplitFile, SplitMode> modeCol;
	@FXML private TableColumn<SplitFile, Number> sizeCol;
	@FXML private TableColumn<SplitFile, Number> partsCol;
	@FXML private TableColumn<SplitFile, String> destCol;

	@FXML private Label lblProgress;
	@FXML private ProgressBar progressbar;

	@FXML private SplitPane splitPane;

	@FXML private Button btnEdit;
	@FXML private Button btnDelete;
	@FXML private Button btnDeleteAll;
	@FXML private Button btnSplit;

	//Reference to the main application
	private MainApp mainApp;

	//Progressbar management
	private int totalSteps = 0;
	private boolean completed;


	/**
	 * Costruttore base.<br>
	 * NOTA: L'inizializzazione vera e propria non avviene qui, ma nel metodo privato {@code initialize()}, che
	 * viene chiamato automaticamente dopo che il file fxml è stato caricato.
	 */
	public HomeController() {}


	/*
	 * Inizializza il controller
	 * Questo metodo viene chiamato automaticamente dopo che il file .fxml è stato caricato
	 */
	@FXML
	private void initialize() {
		//Fix the divider position
		splitPane.getDividers().get(0).positionProperty().addListener(
				(observable, oldValue, newValue) -> splitPane.getDividers().get(0).setPosition(0.7));

		//Rimuove testo dalla lblProgress
		lblProgress.setText("");

		//Disabilita i pulsanti all'inizio, visto che la tabella è vuota
		enableAllButtons(false);

		//Set the table columns data factories
		fileCol.setCellValueFactory( cellData -> cellData.getValue().filenameProperty() );
		modeCol.setCellValueFactory( cellData -> cellData.getValue().splitModeProperty());
		sizeCol.setCellValueFactory( cellData -> cellData.getValue().partSizeProperty() );
		partsCol.setCellValueFactory(cellData -> cellData.getValue().partsNumProperty() );
		destCol.setCellValueFactory( cellData -> cellData.getValue().destPathProperty() );

		//Edit the selected file if double click on a row
		fileTable.setRowFactory(tableView -> {
			TableRow<SplitFile> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty())
					mainApp.showEditFileDialog(fileTable.getSelectionModel().getSelectedItem());
			});
			return row;
		});

		//Nascondi i bottoni 'Modifica' e 'Rimuovi' se nessun elemento è selezionato
		fileTable.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) ->
			enableSingleFileButtons(fileTable.getSelectionModel().getSelectedIndex() >= 0)
		);
	}

	/* Abilita tutti i pulsanti che agiscono silla lista di file da dividere, tranne 'Aggiungi file' */
	public void enableAllButtons(boolean enable) {
		enableSingleFileButtons(enable);
		enableAllFilesButtons(enable);
	}

	/* Abilita i pulsanti che agiscono su tutti i file della lista */
	private void enableAllFilesButtons(boolean enable) {
		btnDeleteAll.setDisable(!enable);
		btnSplit.setDisable(!enable);
	}

	/* Abilita i pulsanti che agiscono solo su un singolo file selezionato della lista */
	private void enableSingleFileButtons(boolean enable) {
		btnEdit.setDisable(!enable);
		btnDelete.setDisable(!enable);
	}


	/**
	 * Imposta un riferimento all'applicazione principale, e collega la lista di file in essa contenuta
	 * alla tabella di questa view..
	 * <p>
	 * Questo metodo viene chiamato dall'applicazione principale per restituire un riferimento a se stessa.
	 */
	public void setMainApp(MainApp app) {
		this.mainApp = app;
		fileTable.setItems(mainApp.getFileList());
	}


	/* Riporta il progresso a zero e resetta il singolo step specificando il numero totale di steps */
	public void resetProgress(int steps) {
		lblProgress.setText("");
		progressbar.setProgress(0);
		totalSteps = steps;
		completed = false;
	}

	/**
	 * Incrementa il progresso di uno step.<br>
	 * Questo metodo viene utilizzato dai thread che dividono i file per incrementare il progresso totale<br>
	 * quando terminano la loro esecuzione.
	 * <p>
	 * NOTA: se il progresso non è stato inizializzato o è già al 100% non fa niente.
	 */
	public synchronized void incProgress() {
		if(completed || totalSteps==0) return;

		progressbar.setProgress(progressbar.getProgress() + 1.0/(double)totalSteps);
		lblProgress.setText("Completamento... " + progressbar.getProgress()*100 + "%");
		if(progressbar.getProgress() == 1)
			lblProgress.setText("Completato");
	}


	/*
	Gestore del pulsante 'Rimuovi tutti'
	Elimina tutti i file dalla lista.
	 */
	@FXML
	private void handleRemoveAll() {
		fileTable.getItems().clear();
		enableAllButtons(false);
	}


	/*
	Gestore del pulsante 'Rimuovi'
	Rimuove il file selezionato dalla lista.
	 */
	@FXML
	private void handleRemove() {
		int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
		if(selectedIndex >= 0) {
			fileTable.getItems().remove(selectedIndex);
			enableAllButtons(!fileTable.getItems().isEmpty());
		}
		/*else
			mainApp.throwAlert(AlertType.WARNING, "Filesplitter", "Nessun file selezionato!",
					"Per rimuovere un file dalla lista devi prima selezionarlo!");*/
	}


	/*
	Gestore del pulsante 'Modifica'
	Modifica il file selezionato nella lista.
	 */
	@FXML
	private void handleEdit() {
		int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
		if(selectedIndex >= 0) {
			mainApp.showEditFileDialog(fileTable.getSelectionModel().getSelectedItem());
		}
		/*else
			mainApp.throwAlert(AlertType.WARNING, "Filesplitter", "Nessun file selezionato!",
					"Per modificare un file devi prima selezionarlo!");*/
	}


	/*
	Gestore del pulsante 'Aggiungi file'
	Aggiunge uno o più file alla lista.
	 */
	@FXML
	private void handleAdd() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		List<File> files = fileChooser.showOpenMultipleDialog(mainApp.getPrimaryStage());

		if(files == null) return;
		//Se è stato selezionato più di un file viene permesso di applicare le opzioni a più file insieme
		boolean multipleFiles = files.size() > 1;
		boolean useModel = false;
		SplitFile model = null;
		for(File f: files) {
			if(useModel) {
				mainApp.getFileList().add(model.getCopy(f));
			}
			else {
				SplitFile tmp = new SplitFile(f, SplitInfo.SplitMode.DEFAULT);
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
		enableAllFilesButtons(true);
	}


	/*
	Gestore del pulsante 'Dividi File'
	Esegue la procedura di split sui file in lista, in maniera concorrente affidando ogni file a un singolo thread.
	 */
	@FXML
	private void handleSplitFiles() {
		mainApp.splitFiles();
	}


	/*
	Gestore del pulsante 'Unisci file'
	Richiama il metodo showMergeFileDialog() dell'applicazione principale.
	 */
	@FXML
	private void handleMergeFiles() {
		mainApp.showMergeFileDialog();
	}
}
