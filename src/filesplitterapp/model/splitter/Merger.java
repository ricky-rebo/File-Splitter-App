package filesplitterapp.model.splitter;

import java.io.*;

/**
 * TODO docs
 * @author Riccardo Rebottini
 */
public class Merger extends FileManipulator {
	//TODO docs
	public Merger(SplitInfo info) {
		super(info);
	}

	//TODO docs
	public void merge() throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] fileBytes;

		for(int i = 0; i<info.getParts(); i++) {
			fileBytes = readPart(getPartFile(info.getWorkspace(), i+1));
			if(fileBytes == null) throw new Exception("Impossibile leggere file " + getPartFile(info.getWorkspace(), i+1).getName());
			buffer.write(fileBytes);

			//System.out.println("> loaded part "+(i+1)+" of "+info.getParts());
		}
		//Check if a file with the same name already exist in that location
		if(info.getFile().exists() && info.getFile().isFile())
			info.setName("merged_"+info.getName());

		writeFile(info.getFile(), buffer.toByteArray());
		buffer.close();
	}

	private void writeFile(File file, byte[] bytes) throws SplitterException {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(file);
			fout.write(bytes);
			fout.close();
		} catch (IOException ex) {
			if(file.exists() && file.isFile()) file.delete();
			throw new SplitterException("Impossibile scrivere file\n"+file.getAbsolutePath(), ex);
		}

	}

	protected byte[] readPart(File file) throws SplitterException {
		return readFile(file);
	}


	//TODO docs
	public void deletePartFiles() {
		for(int i=0; i<info.getParts(); i++) {
			getPartFile(info.getFile().getParent(), i+1).delete();
		}
	}
}