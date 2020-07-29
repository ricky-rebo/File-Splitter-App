package filesplitter.model.splitter;

import java.io.*;

//TODO docs
public class SplitInfo implements Serializable {
    private static final long serialVersionUID = 20191206L;
    /**
     * L'estensione utilizzata dai file su cui questo oggetto viene serializzato.
     */
    public static final String PINFO_EXT = ".pinf";

    private File file;
    private int parts;
    private SplitMode splitMode;
    private String keyHash = null;


    /**
     * Create Create a new SplitInfoContainer object
     * @param file The file to be splitted
     * @param parts the number of parts
     * @param splitMode the split mode used (DEFAULT, ZIP, CRYPTED)
     */
    public SplitInfo(File file, int parts, SplitMode splitMode) {
        this.file = file;
        this.parts = parts;
        this.splitMode = splitMode;
    }


    public File getFile() { return file; }
    public String getName() { return file.getName(); }
    public int getFileSize() {return (int)file.length(); }
    public int getParts() { return parts; }
    public SplitMode getSplitMode() { return splitMode; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }

    public String getInfoFilename() {
        return file.getName().substring(0, file.getName().lastIndexOf('.'))+PINFO_EXT;
    }


    /**
     * Save the current SplitInfo object into a @va file.
     */
    public void save(String saveTo) {
        ObjectOutputStream setInfo = null;
        try {
            setInfo = new ObjectOutputStream(new FileOutputStream(saveTo+'\\'+getInfoFilename()));
            setInfo.writeObject(this);
            setInfo.flush();
            setInfo.close();
        }
        catch(IOException e) {e.printStackTrace();}
    }


    /**
     * Carica un oggetto SplitInfo da un file .pinf esistente.
     *
     * @param infoFile il file .pinf da leggere
     * @return un oggetto SplitInfo
     */
    public static SplitInfo load(File infoFile) throws FileSplitterException {
        if(!infoFile.getName().substring(infoFile.getName().lastIndexOf('.')).equals(PINFO_EXT)) {
            String msg = "Il file selezionato non contiene informazioni su un file diviso.\n" +
                         "Selezionare un file con estensione "+PINFO_EXT+" valido!";
            throw new FileSplitterException(msg, null);
        }

        ObjectInputStream getInfo = null;
        SplitInfo obj = null;
        try {
            getInfo = new ObjectInputStream(new FileInputStream(infoFile.getAbsolutePath()));
            obj = (SplitInfo)getInfo.readObject();
            getInfo.close();
        }
        catch(IOException | ClassNotFoundException ex) {
            String msg = "Impossibile caricare il file\n"+infoFile.toString()+"\n"+
                         "E' possibile che il file sia danneggiato o non valido";
            throw new FileSplitterException(msg, ex);
        }

        obj.file = new File(infoFile.getParent()+'\\'+obj.getName());
        return obj;
    }
}