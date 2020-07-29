package filesplitterapp.model.splitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * {@code Merger} estende la classe {@code FileManipulator} implementando la procedure di merge delle parti in un file unico,<br>
 * sfruttando i metodi di lettura e scrittura già definiti.
 *
 * @author Riccardo Rebottini
 */
public class Merger extends FileManipulator {
	/**
	 * Crea un nuovo oggetto {@code Merger}
	 *
	 * @param info lo {@code SplitInfo} contenente il file da unire e le relative impostazioni con cui è stato diviso
	 */
	public Merger(SplitInfo info) {
		super(info);
	}

	/**
	 * Unisce il file specificato in {@code info}, secondo le impostazioni utilizzate in fase di divisione
	 *
	 * @throws SplitterException in caso si verifichi un errore in fase di lettura delle parti o quando si scrive il file unito
	 */
	public void merge() throws SplitterException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] fileBytes;

		try {
			//Leggo tutte le parti
			for(int i = 0; i<info.getPartsNum(); i++) {
				fileBytes = readFile(getPartFile(info.getWorkspace(), i+1));
				buffer.write(fileBytes);
			}

			//Se un file con lo stesso nome esiste già, rinomino il file unito
			if(info.getFile().exists() && info.getFile().isFile())
				info.setName("merged_"+info.getName());

			//Verifico che l'hash del file risultante sia uguale all'hash del file originale
			info.verifyFile(buffer.toByteArray());

			//Scrivo il file risultante
			writeFile(info.getFile(), buffer.toByteArray());
			buffer.close();
		} catch(IOException ex) {
			throw new SplitterException("Impossibile unire file "+info.getName(), ex);
		}

	}
}