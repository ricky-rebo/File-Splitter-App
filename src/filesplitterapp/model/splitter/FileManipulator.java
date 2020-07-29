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


    /**
     *
     * @param file
     * @return
     */
    protected byte[] readFile(File file) throws SplitterException {
        FileInputStream fis;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        try {
            fis  = new FileInputStream(file);
            while((len = fis.read(buffer)) != -1)
                baos.write(buffer, 0, len);

            fis.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new SplitterException("Impossibile leggere file\n"+file.getAbsolutePath()+
                    "", ex);
        }
    }

    protected void writeFile(File file, byte[] bytes) throws SplitterException {
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
}
