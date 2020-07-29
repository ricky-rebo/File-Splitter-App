package filesplitterapp.model.splitter;

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
public class Splitter extends FileManipulator {
	public enum SplitMode {
		DEFAULT, ZIP, CRYPTO;

		@Override
		public String toString() {
			switch(this) {
				case DEFAULT: return "Split Only";
				case ZIP: return "Split & Zip";
				case CRYPTO:return "Split & Crypt";
			}
			return null;
		}
	}


	//TODO docs
	public Splitter(SplitInfo info) {
		super(info);
	}


	/**
	 * Split the file pointed in the SplitInfo object, with the specified split options
	 * @return true if the split procedure works fine, false otherwise
	 */
	public boolean split() throws SplitterException {
		File saveDir = new File(info.getWorkspace());
		if(!saveDir.exists() || !saveDir.isDirectory()) saveDir.mkdir();

		byte[] bytes = readFile(info.getFile());
		if(bytes == null) { System.out.println("NO BYTES READ"); return false; }

		List<byte[]> parts = partition(bytes, info.getParts());

		for(int i=0; i<parts.size(); i++) {
			System.out.println("> Part "+i+" dim: "+parts.get(i).length);
			writeFile(getPartFile(info.getWorkspace(), i+1), parts.get(i));
		}

		info.save();
		return true;
	}

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

	protected void deleteParts() {
		for(int i=0; i<info.getParts(); i++) {
			File pfile = getPartFile(info.getWorkspace(), i+1);
			if(pfile.exists() && pfile.isFile())
				pfile.delete();
			else return;
		}
	}
}
