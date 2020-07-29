package filesplitterapp.model;

import filesplitterapp.model.splitter.SplitInfo;
import filesplitterapp.model.splitter.SplitInfo.SplitMode;
import javafx.beans.property.*;

import java.io.File;

/**
 * Il modello dati utilizzato dalla tabella per visualizzare i file in coda.
 * Contiene un riferimento ad un file e ne definisce la modalità di divisione, il
 * numero di parti, il percorso di salvataggio di esse e l'eventuale chiave di cifratura.
 *
 * Tutti i dati visualizzabili sono rappresentati da property, oggetti introdotti da JavaFX che estendono
 * i tipi semplici aggiungendo eventi che vengono scatenati ad ogni modifica delle Property stesse, che avviene con i metodi {@code get()} e {@code set()}.
 * Ciò permette di poter tenere aggiornati i dati visualizzati nella GUI.
 *
 * Oltre alle property è anche presente un metodo {@code getSplitInfo()} che ritorna un oggetto {@code SplitInfo} basato sulle<br>
 * impostazioni di divisione qui contenute, che verrà utilizzato dal modulo {@code model.splitter}.
 *
 * @author Rebottini Riccardo
 */
public class SplitFile {
    private final File file;
    private String cryptKey = null;
    private boolean saveToSubDir = false;
    private boolean specsParts = false;

    private ObjectProperty<SplitMode> splitMode;
    private IntegerProperty partsNum;
    private IntegerProperty partSize;
    private StringProperty destPath;


    /**
     * Crea un nuovo oggetto SplitFile, specificando un file e la modalità in cui dividerlo.
     *
     * @param file il file da dividere
     * @param mode come dividere il file
     */
    public SplitFile(File file, SplitMode mode) {
        this.file = file;

        destPath = new SimpleStringProperty(file.getParent());
        splitMode = new SimpleObjectProperty<>(mode);
        partsNum = new SimpleIntegerProperty(1);
        partSize = new SimpleIntegerProperty((int)file.length());
    }


    /** Ritorna il percorso assoluto del file da dividere */
    public String getAbsoluteFilename() { return file.getAbsolutePath(); }

    /** Imposta il percorso in cui salvare le parti del file fiviso */
    public void setDestPath(String path, boolean includeSubDir) {
        saveToSubDir = includeSubDir;
        destPath.set(path);
    }
    /** Ritorna il percorso in cui salvare le parti del file diviso */
    public String getDestPath() { return destPath.get(); }

    /** Ritorna il percorso in cui salvare le parti del file diviso, includendo la subdirectory "[nome_file]_parts" se saveToSubDir = true */
    public String getFinalDestPath() {
        return destPath.get() + (saveToSubDir ? File.separator+file.getName().substring(0, file.getName().lastIndexOf('.'))+"_parts" : "");
    }
    /** Specifica se è stato impostato di salvare le parti in una subdirectory o meno */
    public boolean saveToSubDir() { return saveToSubDir; }

    // splitMode
    /** Imposta la modalità in cui dividere il file */
    public void setSplitMode(SplitMode mode) { splitMode.set(mode); }
    /** Ritorna la modalità in cui dividere il file */
    public SplitMode getSplitMode() { return splitMode.get(); }

    // partSize
    /** Imposta la dimensione delle parti in cui dividere il file, e di conseguenza anche il numero di parti */
    public void setPartSize(int size) {
        partSize.set(size);
        specsParts = false;

        int pnum = (int)file.length() / size;
        if (((int)file.length() % size) > 0) pnum++;
        partsNum.set(pnum);
    }
    /** Ritorna la dimensione delle parti in cui dividere il file */
    public int getPartSize() { return partSize.get(); }

    // partsNum
    /** Imposta il numero di parti in cui dividere il file, e di conseguenza anche la dimensione delle parti */
    public void setPartsNum(int pnum) {
        partsNum.set(pnum);
        specsParts = true;

        int psize = (int)file.length() / pnum;
        if (((int)file.length() % psize) > 0) psize++;
        partSize.set(psize);
    }
    /** Ritorna il numero di parti in cui dividere il file */
    public int getPartsNum() { return partsNum.get(); }

    // cryptKey
    /** Imposta la chiave con cui criptare il file (richiede splitMode = CRYPTO) */
    public void setCryptKey(String key) { this.cryptKey = key; }
    /** Ritorna la chiave con cui criptare il file (richiede splitMode = CRYPTO) */
    public String getCryptKey() { return this.cryptKey; }


    // Property Getters
    /** Ritorna la {@code Property} relativa al nome del file */
    public StringProperty filenameProperty() { return new SimpleStringProperty(file.getName()); }
    /** Ritorna la {@code Property} relativa alla modalità in cui dividere il file */
    public ObjectProperty<SplitMode> splitModeProperty() { return splitMode; }
    /** Ritorna la {@code Property} relativa al numero di parti in cui dividere il file */
    public IntegerProperty partsNumProperty() { return partsNum; }
    /** Ritorna la {@code Property} relativa alla dimensione delle parti in cui dividere il file */
    public IntegerProperty partSizeProperty() { return partSize; }
    /** Ritorna la {@code Property} relativa al percorso in cui salvare le parti */
    public StringProperty destPathProperty() { return destPath; }



    /** Ritorna le impostazioni di split correnti applicate ad un nuovo file */
    public SplitFile getCopy(File file) {
        SplitFile copy = new SplitFile(file, splitMode.get());

        if(specsParts) copy.setPartsNum(partsNum.get());
        else copy.setPartSize(partSize.get());

        copy.setDestPath(destPath.get(), saveToSubDir);
        copy.setCryptKey(cryptKey);
        return copy;
    }


    /** Ritorna un oggetto {@code SplitInfo} basato sulle impostazioni correnti */
    public SplitInfo getSplitInfo(){
        return new SplitInfo(file, getFinalDestPath(), partsNum.get(), partSize.get(), splitMode.get());
    }
}
