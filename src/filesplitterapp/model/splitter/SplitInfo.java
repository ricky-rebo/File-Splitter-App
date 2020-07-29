package filesplitterapp.model.splitter;

import java.io.*;
import filesplitterapp.model.splitter.Splitter.SplitMode;


//TODO docs
public class SplitInfo implements Serializable {
    private static final long serialVersionUID = 20191206L;
    /**
     * L'estensione utilizzata dai file su cui questo oggetto viene serializzato.
     */
    public static final String PINFO_EXT = ".pinf";

    private File file;
    private String workspace;
    private int partsize;
    private int parts;
    private SplitMode splitMode;
    private String keyHash = null;
    private byte[] iv = null; //TODO


    /**
     * Create a new SplitInfoContainer object
     * @param file The file to be splitted
     * @param parts the number of parts
     * @param splitMode the split mode used (DEFAULT, ZIP, CRYPTED)
     */
    public SplitInfo(File file, String workspace,  int parts, int partsize, SplitMode splitMode) {
        this.file = file;
        this.workspace = workspace;
        this.parts = parts;
        this.partsize = partsize;
        this.splitMode = splitMode;
    }


    public File getFile() { return file; }
    public String getName() { return file.getName(); }
    public String getWorkspace() { return workspace; }
    public void setWorkspace(String newWorkspace) { workspace = newWorkspace; } //TODO add newWorkspace validity check?
    public void setFileLocation(String newLoc) { file = new File(newLoc, file.getName()); } //TODO add newLock validity check
    public void setName(String newName) { file = new File(file.getParent(), newName); }
    public int getParts() { return parts; }
    public int getPartSize() { return partsize; }
    public SplitMode getSplitMode() { return splitMode; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public void setIV(byte[] iv) { this.iv = iv; }
    public String getKeyHash() { return keyHash; }
    public byte[] getIV() { return iv; }

    public String getInfoFilename() {
        return file.getName().substring(0, file.getName().lastIndexOf('.'))+PINFO_EXT;
    }


    /**
     * Save the current SplitInfo object into a @va file.
     */
    public void save() {
        ObjectOutputStream setInfo = null;
        try {
            setInfo = new ObjectOutputStream(new FileOutputStream(new File(workspace, getInfoFilename())));
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
    public static SplitInfo load(File infoFile) throws SplitterException {
        if(!infoFile.getName().substring(infoFile.getName().lastIndexOf('.')).equals(PINFO_EXT)) {
            String msg = "Il file selezionato non contiene informazioni su un file diviso.\n" +
                         "Selezionare un file con estensione "+PINFO_EXT+" valido!";
            throw new SplitterException(msg, null);
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
            throw new SplitterException(msg, ex);
        }

        obj.setWorkspace(infoFile.getParent());
        obj.setFileLocation(infoFile.getParent());
        return obj;
    }
}