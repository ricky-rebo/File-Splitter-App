package filesplitterapp.model.splitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

public class ZipMerger extends Merger {
    public ZipMerger(SplitInfo info) {
        super(info);
    }

    @Override
    protected byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        ZipInputStream zis;

        //System.out.println("> Reading zipped part "+file.getAbsolutePath()+" (dim: "+file.length()+")");

        zis = new ZipInputStream(new FileInputStream(file));
        zis.getNextEntry();

        while((len=zis.read(buffer)) != -1){
            //System.out.println(("> Read a chunk of "+len+" from the part "+file.getName())+" (buffer total size: "+buffer.length+")");
            baos.write(buffer, 0, len);
        }

        zis.closeEntry();
        zis.close();

        //System.out.println("> Read data dim: "+baos.size());
        return baos.toByteArray();
    }
}
