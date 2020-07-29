package filesplitterapp.model.splitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipSplitter extends Splitter {
    //TODO docs
    public ZipSplitter(SplitInfo infos) {
        super(infos);
    }


    @Override
    protected void writePart(byte[] part, File file) throws SplitterException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(file));
            zos.putNextEntry(new ZipEntry(file.getName()));
            zos.write(part);
            zos.closeEntry();
            zos.close();
        }
        catch(IOException ex) {
            deleteParts();
            throw new SplitterException("Impossibile scrivere file\n"+file.getAbsolutePath()+"\n\nFile "+info.getName()+" non diviso", ex);
        }
    }
}
