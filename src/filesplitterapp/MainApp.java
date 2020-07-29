package filesplitterapp;

import filesplitterapp.model.SplitFile;
import filesplitterapp.model.SplitThread;
import filesplitterapp.model.splitter.*;
import filesplitterapp.view.EditFileDialogController;
import filesplitterapp.view.HomeController;
import filesplitterapp.view.MergeFileDialogController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.security.InvalidKeyException;


/**
 * Classe principale, che estende {@code Application} di JavaFX, rappresenta la base di questa applicazione.
 * <p>
 * Il metodo statico {@code main()} si limita a chiamare il metodo statico {Application.launch(String... args)}, che lancia l'applicazione,<br>
 * eseguendo queste operazioni:<br>
 * - Crea un'istanza dell'applicazione<br>
 * - Chiama il metodo {@code init()}<br>
 * - Chiama il metodo {@code start(Stage primaryStage)}, il vero e proprio punto di partenza dell'applicazione<br>
 * - Aspetta che l'applicazione termini (viene chiamato {@code Platform.exit()} oppure ogni finestra viene chiusa)<br>
 * - Chiama il metodo {@code stop()}<br>
 * <p>
 * In questo caso non viene eseguita alcuna operazione di inizializzazione ma viene lasciato tutto il lavoro a {@code start()} perchè questo metodo,<br>
 * a differenza di {@code init()}, viene eseguito dal thread di JavaFX, l'unico che può operare sui suoi componenti "live".
 * <p>
 * Questa classe contiene anche i metodi relativi ale varie view, che ne caricano i rispettivi file {@code FXML} e le visualizzano, e altre funzioni<br>
 * utili all'applicazione stessa.
 *
 * @author Riccardo Rebottini
 */
public class MainApp extends Application {
	/** Percorso in cui salvare i file di log */
	public static final String LOG_PATH = System.getProperty("user.home") + File.separator + "FileSplitterApp" + File.separator + "logs";

	private Stage primaryStage;
	private ObservableList<SplitFile> fileList = FXCollections.observableArrayList();
	private HomeController homeController;

	
	/** Costruttore base dell'applicazione. */
	public MainApp() {}


	/**
	 * Punto di partenza vero e proprio dell'applicazione.
	 * <p>
	 *     Salva lo {@code stage} che viene passato come parametro, detto {@code primaryStage} visto che è quello principale,
	 *     imposta titolo e icona dell'applicazione, e visualizza il pannello principale, tramite il metodo {@code showHomePane()}.
	 * </p>
	 */
	@Override
	public void start(Stage primaryStage) {
		//Salvo un riferimento dello stage su cui viene visualizzata l'applicazione
		this.primaryStage = primaryStage;

		//Imposto il titolo dell'applicazione
		this.primaryStage.setTitle("File Splitter");

		//Imposto l'icona dell'applicazione
		this.primaryStage.getIcons().add(new Image("file:resources/images/icon-512.png"));

		//Creo la cartella di log dell'applicazione, se non esiste già, all'interno della cartella utente
		File logLoc = new File(LOG_PATH);
		if(!logLoc.exists() || !logLoc.isDirectory())
			logLoc.mkdirs();

		//Mostro il pannello principale
		showHomePane();
	}


	/**
	 * Carica e visualizza il pannello principale, e ottiene l'istanza del relativo controller, <br>
	 * per fornirgli un riferimento all'applicazione stessa.
	 */
	public void showHomePane() {
		try {
			//Crea un FXMLLoader e carica il pannello
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/Home.fxml"));
			AnchorPane mainPane = loader.load();

			//Crea una scena e la imposta nel primaryStage
			Scene scene = new Scene(mainPane);
			primaryStage.setScene(scene);

			//Visualizza il primaryStage
			primaryStage.show();

			//Ottiene l'istanza del controller e gli fornisce un riferimenso all'applicazione stessa
			homeController = loader.getController();
			homeController.setMainApp(this);
		}
		catch(IOException ex) {
			throwAlert(Alert.AlertType.ERROR, "Fatal Error", "Impossibile caricare una componente fondamentale dell'applicazione",
					"Non è stato possibile caricare il file\n"+
					MainApp.class.getResource("view/Home.fxml").toString()+"\n"+
					"Impossibile avviare l'applicazione!");
			Platform.exit();
		}
	}


	/** Ritorna il {@code primaryStage} dell'applicazione */
	public Stage getPrimaryStage() {
		return primaryStage;
	}


	/** Ritorna l'{@code ObservableList} contenente i file da dividere attualmente caricati dal programma */
	public ObservableList<SplitFile> getFileList() {
		return fileList;
	}


	/**
	 * Crea una dialog per modificare le impostazioni di un file, passato come parametro.
	 */
	public void showEditFileDialog(SplitFile splitFile) {
		showEditFileDialog(splitFile, false);
	}

	/**
	 * Crea un dialog per modificare le impostazioni di divisione di un file, e da la possibilità di applicare le stesse applicazioni a più file.
	 * <p>
	 * L'opzione 'Applica a tutti' viene utilizzata in fase di aggiunta di nuovi file, se l'utente seleziona più file in una sola volta.<br>
	 * In questo modo vengono specificate le impostazioni per un file, e selezionando 'Applica a tutti' le stesse impostazioni verranno applicate<br>
	 * anche a tutti file seguenti in lista.
	 *
	 * @param splitFile il file da dividere
	 * @param allowMultiple specifica se abilitare o meno l'opzione 'Applica a tutti'
	 * @return un array di boolean contenente due flag:<br>
	 * - [0] true se l'utente ha cliccato 'OK', false altrimenti
	 * - [1] true se l'utente ha selezionato 'Applica a tutti', false altrimenti
	 */
	public boolean[] showEditFileDialog(SplitFile splitFile, boolean allowMultiple) {
		try {
			//create the stage and load te pane
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Edit file split options");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/EditFileDialog.fxml"));

			dialogStage.setScene(new Scene(loader.load()));

			//Create the controller and sets the SplitInfo into it
			EditFileDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMainApp(this);
			controller.setSplitFile(splitFile);
			controller.allowApplyToAll(allowMultiple);

			//Show the dialog and wait until the user close it
			dialogStage.showAndWait();

			return new boolean[]{controller.isOkClicked(), controller.applyToAllSelected()};
		}
		catch(IOException ex) {
			Platform.exit();
		}
		return new boolean[0];
	}


	/**
	 * Carica e visualizza la finestra per riunire un file precedentemente diviso.
	 * <p>
	 * Dopo aver caricato il file FXML relativo a questa view, viene chiamato il metodo {@code chooseInfoFile()} del relativo controller, <br>
	 * e se viene selezionato un file viene visualizzata la finestra per selezionare il percorso in cui salvare il file unito, eventualmente<br>
	 * la chiave di decifratura, e completare la procedura di merge.
	 */
	public void showMergeFileDialog() {
		try {
			//Creo il dialogStage
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Unisci file");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);

			//Carico la view
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/MergeFileDialog.fxml"));

			//Imposto la view all'interno del dialogStage
			dialogStage.setScene(new Scene(loader.load()));

			//Ottengo il controller e gli passo il dialogStage
			MergeFileDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMainApp(this);

			//Lancio il FileChooser per selezionare un file .pinf da unire
			//Se l'utente seleziona effettivamente un file mostro il dialog
			if(controller.chooseInfoFile(primaryStage))
				dialogStage.showAndWait();

		}
		catch (IOException ex) {
			Platform.exit();
		}
	}


	/**
	 * Crea e visualizza un {@code FileChooser} per selezionare una directory nel file system.
	 * @param initDir la directory iniziale da cui i {@code FileChooser} deve partire
	 * @param dialogStage lo stage 'padre' del {@code FileChooser}
	 * @return Il percorso della directory selezionata, se l'utente ne ha effettivamente selezionata una, altrimenti {@code null}
	 */
	public String chooseDirectory(String initDir, Stage dialogStage) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Seleziona Cartella");
		File initDirFile = new File(initDir);
		if(!initDirFile.isDirectory()) initDirFile = new File(System.getProperty("user.home"));

		dirChooser.setInitialDirectory(initDirFile);
		File selPath = dirChooser.showDialog(dialogStage);
		return selPath == null ? null : selPath.getAbsolutePath();
	}


	/**
	 * Crea e visualizza un alert.
	 *
	 * @param type Il tipo di alert (Alert.AlertType)
	 * @param title Il titolo dell'alert
	 * @param headerText L'header text dell'alert
	 * @param contentText Il content text dell'alert
	 * @see javafx.scene.control.Alert.AlertType
	 */
	public void throwAlert(Alert.AlertType type, String title, String headerText, String contentText) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.showAndWait();
	}


	/**
	 * Crea un file di log in cui scrive per intero un'eccezione
	 * @param fname il nome del file di log
	 * @param ex l'eccezione da scrivere nel file di log
	 */
	public void logError(String fname, Exception ex) {
		try {
			ex.printStackTrace(new PrintStream(new FileOutputStream(new File(LOG_PATH, fname+".log"))));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Divide i file in lista, affidando ogni singolo file a un thread concorrente.
	 * <p>
	 * Quando tutti i thread hanno terminato verifica i file che sono stati divisi con successo e li elimina dalla lista,
	 * quelli che hanno riscontrato errori invece vengono segnalati all'utente, e per ognuno viene creato un file di log nella cartella
	 * 'logs' dell'applicazione che riporta l'errore che si è verificato
	 */
	public void splitFiles() {
		int listDim = fileList.size();
		SplitThread threads[] = new SplitThread[listDim];
		String failed = "";
		homeController.resetProgress(listDim);

		//Creazione threads
		for(int i=0; i<listDim; i++) {
			threads[i] = new SplitThread(fileList.get(i), () -> homeController.incProgress());
			threads[i].start();
		}

		//Join per attendere che tutti abbiano finito
		for(int i=0; i<listDim; i++) {
			try { threads[i].join(); }
			catch (InterruptedException ex) {
				String fname = threads[i].getSplitFile().filenameProperty().get();
				failed += "- " + fname + "\n";
				logError("split-thread-error_"+fname, ex);
			}
		}

		//Rimozione file completati o eventuale segnalazione errori
		for(int i=0; i<listDim; i++) {
			if(threads[i].isSplit())
				fileList.remove(threads[i].getSplitFile());
			else {
				String fname = threads[i].getSplitFile().filenameProperty().get();
				failed += "- " + fname + "\n";
				logError("split-error_"+fname, threads[i].getException());
			}
		}

		if(failed.length() == 0)
			throwAlert(Alert.AlertType.INFORMATION, "Split Files", "Operazione completata",
					"Tutti i file sono stati divisi con successo.");
		else
			throwAlert(Alert.AlertType.WARNING, "Split Files", "Attenzione",
					"I seguenti file non sono stati divisi:\n\n"+failed);

		homeController.enableAllButtons(!fileList.isEmpty());
	}


	/**
	 * Unisce un file, le cui informazioni sono state caricate da un file {@value filesplitterapp.model.splitter.SplitInfo#PINFO_EXT}
	 *
	 * @param info il file da unire
	 * @param key la chiave di decifratura, se il file è cifrato, altrimenti ""
	 * @param deleteFiles se eliminare le parti e il file con le informazioni a procedura di merge completata correttamente
	 *
	 * @return true se la procedura è stata eseguita correttamente, altrimenti false
	 */
	public boolean mergeFile(SplitInfo info, String key, boolean deleteFiles) {
		Merger merger;
		try {
			switch(info.getSplitMode()) {
				case DEFAULT: merger = new Merger(info); break;
				case ZIP: merger = new ZipMerger(info); break;
				case CRYPTO: merger = new CryptoMerger(info, key); break;
				default: throw new SplitterException("Modalità non valida! Impossibile unire il file\n"+info.getFile().getAbsolutePath());
			}

			//Chiamata alla procedura di merge
			merger.merge();

			if(deleteFiles) {
				merger.deleteParts();
				info.deleteInfoFile();
			}

			throwAlert(Alert.AlertType.INFORMATION, "Merge File", "File ricomposto correttamente",
					"Il file "+info.getName()+" è stato ricomposto correttamente.");
			return true;
		}
		catch (InvalidKeyException ex) {
			throwAlert(Alert.AlertType.ERROR, "Merge file", "Errore!", ex.getMessage());
			return false;
		}
		catch (SplitterException ex) {
			throwAlert(Alert.AlertType.ERROR, "Merge file", "Errore!", ex.getMessage());
			logError("merge-error_"+info.getName(), ex);
			return false;
		}
	}


	public static void main(String[] args) {
		launch(args);
	}
}
