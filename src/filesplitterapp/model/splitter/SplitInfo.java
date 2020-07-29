package filesplitterapp.model.splitter;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * {@code SplitInfo} contiene tutte le informazioni necessarie a {@code Splitter} e {@code Merger} per operare su un file.
 * <p>
 * All'interno è contenuta anche la modalità in cui il file deve essre/è stato diviso, necessario per sapere quale<br>
 * {@code Splitter} o {@code Merger} utilizzare, e le informazioni di cifratura, cioè l'hash della chiave e il parametro
 * {@code IV}, necessari in caso si operi in modalità {@code CRYPTO}.
 *
 * @author Riccardo Rebottini
 */
public class SplitInfo implements Serializable {
    private static final long serialVersionUID = 20191206L;

    /** L'estensione utilizzata dai file su cui questo oggetto viene serializzato. */
    public static final String PINFO_EXT = ".pinf";

    /** Algoritmo utilizzato per calcolare l'hash */
    private static final String HASH_ALG = "MD5";

    /** Enumeratore che contiene le modalità disponibili in cui è possibile dividere un file */
    public enum SplitMode {
        /** Nella modalità DEFAULT il file viene semplicemente diviso in parti */
        DEFAULT,
        /** Nella modalità ZIP il file viene diviso e le singole parti vengono compresse */
        ZIP,
        /** Nella modalità CRYPTO il file viene diviso e le singole parti vengono cifrate */
        CRYPTO;

        @Override
        public String toString() {
            switch(this) {
                case DEFAULT: return "Split Only";
                case ZIP: return "Split & Zip";
                case CRYPTO:return "Split & Crypt";
            }
            return null;
        }
    }


    private File file;
    private String workspace;
    private int partSize;
    private int partsNum;
    private SplitMode splitMode;

    private String keyHash = null;
    private byte[] iv = null;
    private String fileHash = null;


    /**
     * Crea un nuovo oggetto {@code Splitinfo}
     * @param file il file da dividere
     * @param partsNum il numero di parti in cui dividere il file
     * @param splitMode la modalità in cui dividere il file (DEFAULT, ZIP, CRYPTO)
     */
    public SplitInfo(File file, String workspace, int partsNum, int partSize, SplitMode splitMode) {
        this.file = file;
        this.workspace = workspace;
        this.partsNum = partsNum;
        this.partSize = partSize;
        this.splitMode = splitMode;
    }


    /** Ritorna il file su cui lavorare */
    public File getFile() { return file; }
    /** Modifica la posizione del file, mantenendo uguale il nome */
    public void setFileLocation(String newLoc) { file = new File(newLoc, file.getName()); }

    /** Ritorna il nome del file su cui lavorare */
    public String getName() { return file.getName(); }
    /** Modifica il nome del file su cui lavorare, lasciando uguale la sua posizione */
    public void setName(String newName) { file = new File(file.getParent(), newName); }

    /** Ritorna il percorso in cui si trovano o devono essere salvate le parti */
    public String getWorkspace() { return workspace; }
    /** Modifica il percorso in cui si trovano p devono essere salvate le parti */
    public void setWorkspace(String newWorkspace) { workspace = newWorkspace; }

    /** Ritorna il numero di parti in cui dividere/è stato diviso il file */
    public int getPartsNum() { return partsNum; }

    /** Ritorna la dimensione della singola parte (l'ultima potrebbe avere dimensioni minori) */
    public int getPartSize() { return partSize; }

    /** Ritorna la modalità in cui il file è stato/deve essere diviso */
    public SplitMode getSplitMode() { return splitMode; }

    ///** Imposta l'hash della chiave utilizzata per criptare il file (in caso si utilizzi {@code CryptoSplitter}) */
    //public String getKeyHash() { return keyHash; }
    /** Ritorna l'hash della chiave utilizzata per criptare il file (in caso si utilizzi {@code CryptoSplitter}) */
    public void setKeyHash(byte[] pswBytes) throws SplitterException {
        try {
            String key = calcHash(pswBytes);
            this.keyHash = calcHash(key.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new SplitterException("Impossibile generare l'hash della chiave fornita.", ex);
        }

    }

    /** Ritorna il parametro {@code IV} utilizzato dal {@code Cipher} in fase di cifratura del file */
    public byte[] getIV(){ return iv; }
    /** Imposta il parametro {@code IV} utilizzato dal {@code Cipher} in fase di cifratura del file */
    public void setIV(byte[] iv) { this.iv = iv; }

    private String getInfoFilename() {
        return file.getName().substring(0, file.getName().lastIndexOf('.'))+PINFO_EXT;
    }

    ///** Ritorna l'hash del file */
    //private String getFileHash() { return fileHash; }
    /** Imposta l'hash del file */
    public void setFileHash(byte[] content) throws SplitterException {
        try {
            fileHash = calcHash(content);
        } catch (NoSuchAlgorithmException ex) {
            throw new SplitterException("Impossibile calcolare l'hash del file.", ex);
        }
    }

    /** Cancella il file {@value #PINFO_EXT} su cui questo oggetto è stato salvato, se esiste */
    public void deleteInfoFile() {
        File finfo = new File(workspace, getInfoFilename());
        if(finfo.exists() && finfo.isFile()) finfo.delete();
    }


    /* Calcola l'hash di un contenuto, secondo l'algoritmo specificato nella costante HASH_ALG */
    private String calcHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md;
        md = MessageDigest.getInstance(HASH_ALG);
        md.update(bytes);
        return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();

    }


    /**
     * Confronta una chiave con quella memorizzata.
     *
     * @throws InvalidKeyException in caso le due chiavi non siano uguali
     */
    public void verifyKey(byte[] pswBytes) throws InvalidKeyException, SplitterException {
        try {
            String key = calcHash(pswBytes);
            if (!calcHash(key.getBytes()).equals(keyHash))
                throw new InvalidKeyException("Chiave inserita non valida!");
        } catch (NoSuchAlgorithmException ex) {
            throw new SplitterException("Impossibile verificare la chiave fornita.", ex);
        }
    }


    /**
     * Confronta l'hash di un contenuto con quello memorizzato.
     *
     * @throws SplitterException in caso i due contenuti non siano uguali
     */
    public void verifyFile(byte[] content) throws SplitterException {
        try {
            if (!calcHash(content).equals(fileHash))
                throw new SplitterException("File " + file.getName() + " non unito!\n" + "Hash non conforme al file originale!");
        } catch (NoSuchAlgorithmException ex) {
            throw new SplitterException("Impossibile verificare il contenuto del file.", ex);
        }

    }


    /**
     * Salva l'oggetto in un file con estensione {@value #PINFO_EXT}
     *
     * @throws IOException in caso si verifichino errori durante la scrittura del file con le informazioni di divisione
     */
    public void save() throws IOException {
        ObjectOutputStream setInfo;
        setInfo = new ObjectOutputStream(new FileOutputStream(new File(workspace, getInfoFilename())));
        setInfo.writeObject(this);
        setInfo.flush();
        setInfo.close();
    }


    /**
     * Carica e restituisce un oggetto SplitInfo da un file {@value #PINFO_EXT} esistente, e modifica il campo {@code workspace} e la posizione
     * del file intero, ricollocandoli di default nella stessa posizione del file {@value #PINFO_EXT} letto.
     *
     * @param infoFile il file {@value #PINFO_EXT} da leggere
     * @return un oggetto SplitInfo
     * @throws SplitterException in caso il file non abbia estensione {@value #PINFO_EXT} oppure
     * il programma non lo riesca a deserializzare
     */
    public static SplitInfo load(File infoFile) throws SplitterException {
        if(!infoFile.getName().substring(infoFile.getName().lastIndexOf('.')).equals(PINFO_EXT)) {
            String msg = "Il file selezionato non contiene informazioni su un file diviso.\n" +
                         "Selezionare un file con estensione "+PINFO_EXT+" valido!";
            throw new SplitterException(msg, null);
        }

        ObjectInputStream getInfo;
        SplitInfo obj;
        try {
            getInfo = new ObjectInputStream(new FileInputStream(infoFile.getAbsolutePath()));
            obj = (SplitInfo)getInfo.readObject();
            getInfo.close();
        }
        catch(IOException | ClassNotFoundException ex) {
            String msg = "Impossibile caricare il file\n"+infoFile.getAbsolutePath()+"\n"+
                         "E' possibile che il file sia danneggiato o non valido";
            throw new SplitterException(msg, ex);
        }

        obj.setWorkspace(infoFile.getParent());
        obj.setFileLocation(infoFile.getParent());
        return obj;
    }


}