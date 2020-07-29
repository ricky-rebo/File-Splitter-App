package filesplitterapp.model.splitter;

import java.io.*;

public abstract class FileManipulator {
    protected static final String PART_EXT = ".par";
    protected static final int BUFFER_SIZE = 4096;
    protected SplitInfo info;

    public FileManipulator(SplitInfo info) { this.info = info; }

    //TODO docs
    protected File getPartFile(String dest, int i) {
        String filename = info.getName();
        return new File(dest,filename.substring(0, filename.lastIndexOf('.'))+PART_EXT+i);
    }


    //TODO docs
    public void deleteParts() {
        for(int i=0; i<info.getParts(); i++) {
            File pfile = getPartFile(info.getWorkspace(), i+1);
            if(pfile.exists() && pfile.isFile())
                pfile.delete();
            else return;
        }
    }


    //TODO docs
    protected byte[] readFile(File file) throws IOException {
        FileInputStream fis;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        fis  = new FileInputStream(file);
        while((len = fis.read(buffer)) != -1)
            baos.write(buffer, 0, len);

        fis.close();
        return baos.toByteArray();
    }


    //TODO docs
    protected void writeFile(File file, byte[] bytes) throws IOException {
        FileOutputStream fout;
        fout = new FileOutputStream(file);
        fout.write(bytes);
        fout.close();

    }
}
