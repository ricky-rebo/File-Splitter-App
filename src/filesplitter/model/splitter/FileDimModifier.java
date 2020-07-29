package filesplitter.model.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class FileDimModifier {
    protected static final String PART_EXT = ".par";
    protected static final String SEPARATOR = File.separator;
    protected SplitInfo info;

    public FileDimModifier() { info = null; }

    FileDimModifier(SplitInfo info) { this.info = info; }

    //TODO docs
    protected File getPartFile(String dest, int i) {
        String filename = info.getName();
        return new File(dest+SEPARATOR+filename.substring(0, filename.lastIndexOf('.'))+PART_EXT+i);
    }


    /**
     *
     * @param file
     * @return
     */
    protected byte[] readFile(File file) {
        try {
            //TODO re-write read file method
            return Files.readAllBytes(Paths.get(file.getAbsolutePath()));

        }
        catch(IOException e) {
            e.printStackTrace();
            System.out.println("fileToBytes ERROR (file: "+file.toString()+")");
            return null;
        }
    }
}
