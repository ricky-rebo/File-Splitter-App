package filesplitterapp.model.splitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
	public void merge() throws SplitterException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] fileBytes;

		try {
			for(int i = 0; i<info.getParts(); i++) {
				fileBytes = readFile(getPartFile(info.getWorkspace(), i+1));
				buffer.write(fileBytes);

				//System.out.println("> loaded part "+(i+1)+" of "+info.getParts());
			}
			//Check if a file with the same name already exist in that location
			if(info.getFile().exists() && info.getFile().isFile())
				info.setName("merged_"+info.getName());

			writeFile(info.getFile(), buffer.toByteArray());
			buffer.close();
		} catch(IOException ex) {
			throw new SplitterException("Impossibile unire file "+info.getName(), ex);
		}

	}


	//TODO docs
	public void deletePartFiles() {
		for(int i=0; i<info.getParts(); i++) {
			getPartFile(info.getFile().getParent(), i+1).delete();
		}
	}
}