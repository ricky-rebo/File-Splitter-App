package filesplitterapp.model.splitter;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code Splitter} estende la classe astratta {@code FileManipulator}, implementando il partizionamento del file<br>
 * e la procedura di split, utilizzando i metodi di lettura e scrittura di un file forniti dalla classe astratta.
 *
 * @author Riccardo Rebottini
 */
public class Splitter extends FileManipulator {
	/**
	 * Crea un nuovo oggetto {@code Splitter}
	 *
	 * @param info lo {@code SplitInfo} contenente il file da dividere e le relative impostazioni di divisione
	 */
	public Splitter(SplitInfo info) {
		super(info);
	}


	/**
	 * Divide il file secondo le impostazioni contenute in {@code info}
	 *
	 * @throws SplitterException se si verificano errori in fase di lettura o quando si tenta di scrivere una una parte o il file contenente le informazioni di divisione
	 */
	public void split() throws SplitterException {
		//Se non esiste la cartella in cui salvare i file la creo
		File saveDir = new File(info.getWorkspace());
		if(!saveDir.exists() || !saveDir.isDirectory()) saveDir.mkdirs();

		byte[] bytes;
		List<byte[]> parts;

		try {
			//Leggo il contenuto del file
			bytes = readFile(info.getFile());

			//Salvo l'hash del file
			info.setFileHash(bytes);

			//Partiziono il file
			parts = partition(bytes, info.getPartsNum());

			//Scrivo le parti
			for(int i=0; i<parts.size(); i++) {
				writeFile(getPartFile(info.getWorkspace(), i+1), parts.get(i));
			}

			//Scrivo il file contenente le informazioni di divisione
			info.save();
		}
		catch(IOException ex) {
			deleteParts();
			info.deleteInfoFile();
			throw new SplitterException("Impossibile dividere file "+info.getFile().getAbsolutePath(), ex);
		}


	}

	/*
	Prende il contenuto di un intero file, in formato byte array, e ritorna una lista di byte array, che rappresentano
	le parti da scrivere.
	 */
	private List<byte[]> partition(byte[] bytes, int partsnum) {
		List<byte[]> parts = new ArrayList<>();
		int partSize = info.getPartSize();

		for(int i=0; i<partsnum; i++) {
			int dim = ((i==(partsnum-1)) && ((bytes.length%partSize)>0)) ?
					(bytes.length-(i*partSize)) : partSize;
			parts.add(Arrays.copyOfRange(bytes, i*partSize, (i*partSize)+dim));
		}

		return parts;
	}
}
