package filesplitterapp;

import filesplitterapp.model.SplitFile;
import filesplitterapp.model.splitter.Splitter.SplitMode;
import filesplitterapp.view.EditFileDialogController;
import filesplitterapp.view.HomeController;
import filesplitterapp.view.MergeFileDialogController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


/**
 * The main class of this application
 * @author Riccardo Rebottini 
 *
 */
public class MainApp extends Application {
	private Stage primaryStage;
	private ObservableList<SplitFile> fileList = FXCollections.observableArrayList();

	
	/**
	 * The base constructor.
	 * Adds some dummy data, just for test.
	 */
	public MainApp() {
		//fileList.add(new SplitFile(new File("C:\\Users\\ricky\\eclipse-workspace\\FileSeparator\\test-files\\TEOTFW.png"), SplitMode.DEFAULT));
		//fileList.add(new SplitFile(new File("C:\\Users\\ricky\\eclipse-workspace\\FileSeparator\\test-files\\CarRadio.txt"), SplitMode.ZIP));
		//fileList.add(new SplitFile(new File("C:\\Users\\ricky\\eclipse-workspace\\FileSeparator\\test-files\\CarRadio_merged.txt"), SplitMode.CRYPTO));

		SplitFile file = new SplitFile(new File("C:\\Users\\ricky\\eclipse-workspace\\FileSeparator\\test-files\\CarRadio.txt"), SplitMode.CRYPTO);
		file.setCryptKey("test");
		file.setDestPath("C:\\Users\\ricky\\Desktop\\split_files\\new_crypto", true);
		file.setPartsNum(3);
		fileList.add(file);
	}


	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("File Splitter");

		showMainPane();
	}


	/**
	 * Loads the main pane and shows it
	 */
	public void showMainPane() {
		try {
			//Load person overview
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/Home.fxml"));
			AnchorPane mainPane = (AnchorPane) loader.load();

			Scene scene = new Scene(mainPane);
			primaryStage.setScene(scene);
			primaryStage.show();

			//ADD - Give the controller access to the main app
			HomeController controller = loader.getController();
			controller.setMainApp(this);
		}
		catch(IOException ex) {ex.printStackTrace();}
	}


	/**
	 * Returns the primary stage
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}


	/**
	 * Return the ObservableList containing the files currently loaded
	 */
	public ObservableList<SplitFile> getFileList() {
		return fileList;
	}


	/**
	 * Create a edit file options dialog, with a specific SplitInfo object.
	 *
	 * @param splitFile the SplitInfo object to edit
	 * @param allowMultiple true to allow the user to apply options to multiple files, false otherwise
	 * @return A boolean array representing two flags:
	 * 	 [0] -> true if the user has clicked the OK button, false otherwise
	 * 	 [1] -> true if the user has checked the 'apply to all' check box, false otherwise (require allowMultiple = true)
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
			controller.setSplitFile(splitFile);
			controller.allowMultipleApply(allowMultiple);

			//Show the dialog and wait until the user close it
			dialogStage.showAndWait();

			return new boolean[]{controller.isOkClicked(), controller.applyMultiple()};
		}
		catch(IOException ex) {
			ex.printStackTrace();
			return new boolean[]{false, false};
		}
	}


	//TODO docs
	public void showMergeFileDialog() {
		try {
			//create the stage and load the pane
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Unisci file");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/MergeFileDialog.fxml"));

			dialogStage.setScene(new Scene(loader.load()));

			//Create the controller and sets the stage
			MergeFileDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);


			if(controller.chooseInfoFile(primaryStage))
				dialogStage.showAndWait();

		}
		catch (IOException ex) { ex.printStackTrace(); }
	}


	public static void main(String[] args) {
		launch(args);
	}
}
