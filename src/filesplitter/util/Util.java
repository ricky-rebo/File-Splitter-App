package filesplitter.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
	/**
	 * Throw a new alert popup.
	 *
	 * @param type the alert type
	 * @param title the alert title
	 * @param headerText the header text
	 * @param contentText the content text
	 */
	public static void throwAlert(AlertType type, String title, String headerText, String contentText) {
		Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
	}

	public static String calcMD5(byte[] bytes) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			return DatatypeConverter
					.printHexBinary(md.digest()).toUpperCase();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String chooseDirectory(String initDir, Stage dialogStage) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Seleziona Cartella");
		File initDirFile = new File(initDir);
		if(!initDirFile.isDirectory()) initDirFile = new File(System.getProperty("user.home"));

		dirChooser.setInitialDirectory(initDirFile);
		File selPath = dirChooser.showDialog(dialogStage);
		return selPath == null ? null : selPath.getAbsolutePath();
	}

	public static String remExt(String name) {
		return name.substring(0, name.lastIndexOf('.'));
	}
}
