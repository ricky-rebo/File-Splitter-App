package filesplitter.model.splitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Riccardo Rebottini
 * @version 2.0
 */
public class FileSplitter extends FileDimModifier {
	protected int partSize;


	//TODO docs
	public FileSplitter(SplitInfo infos) {
		super(infos);
		partSize = (int) infos.getFileSize() / infos.getParts();
	}


	/**
	 * Split the file pointed in the SplitInfo object, with the specified split options
	 * @return true if the split procedure works fine, false otherwise
	 */
	public boolean split(String saveTo) {
		File saveDir = new File(saveTo);
		if(!saveDir.exists() || !saveDir.isDirectory()) saveDir.mkdir();

		byte[] bytes = readFile(info.getFile());
		if(bytes == null) {//System.out.println("NO BYTES READ");
		return false;}

		List<byte[]> parts = partition(bytes, info.getParts());

		for(int i=0; i<parts.size(); i++) {
			writePart(parts.get(i), getPartFile(saveTo, i+1));
		}

		info.save(saveTo);
		return true;
	}


	protected void writePart(byte[] part, File file) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(part);
			fos.close();
		}
		catch(IOException e) {
			//TODO aggiungere rimozione parti già scritte in caso di errore
			e.printStackTrace();
		}
	}


	private List<byte[]> partition(byte[] bytes, int partsnum) {
		List<byte[]> parts = new ArrayList<>();

		for(int i=0; i<partsnum; i++) {
			int dim = ((i==(partsnum-1)) && ((bytes.length%partSize)>0)) ?
					(bytes.length-(i*partSize)) : partSize;
			parts.add(Arrays.copyOfRange(bytes, i*partSize, (i*partSize)+dim));
		}

		return parts;
	}
}
