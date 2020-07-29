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
			fileBytes = readFile(getPartFile(info.getWorkspace(), i+1));
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


	//TODO docs
	public void deletePartFiles() {
		for(int i=0; i<info.getParts(); i++) {
			getPartFile(info.getFile().getParent(), i+1).delete();
		}
	}
}