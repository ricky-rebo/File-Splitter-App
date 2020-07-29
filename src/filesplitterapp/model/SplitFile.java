package filesplitterapp.model;

import filesplitterapp.model.splitter.SplitInfo;
import filesplitterapp.model.splitter.SplitMode;
import filesplitterapp.util.Util;
import javafx.beans.property.*;

import java.io.File;

/**
 * Il modello dati principale di questa applicazione.
 * Contiene un riferimento ad un file e ne definisce la modalità di divisione, il
 * numero di parti, il percrso di salvataggio di esse e l'eventuale chiave di cifratura.
 *
 * Tutti i dati sono rappresentati da property, oggetti introdotti da JavaFX che estendono
 * i tipi semplici aggiungeno eventi che vengono scatenati ad ogni modifica delle Property.
 * Ciò permette di poter tenere aggiornati i dati visualizzati nella GUI.
 * @author Rebottini Riccardo
 *
 */
public class SplitFile {
    private final File file;
    private String cryptKey = null;
    private boolean saveToSubDir = false;

    private ObjectProperty<SplitMode> splitMode = null;
    private IntegerProperty partsNum = null;
    private IntegerProperty partSize = null;
    private StringProperty destPath = null;


    /**
     * Default constructor
     */
    public SplitFile() {
        file = null;
    }


    /**
     * Create a new SplitFile object
     *
     * @param file a file object
     * @param mode the split mode you want to use. It can be DEFAULT, ZIP or CRYPT
     */
    public SplitFile(File file, SplitMode mode) {
        this.file = file;

        destPath = new SimpleStringProperty(file.getParent());
        splitMode = new SimpleObjectProperty<SplitMode>(mode);
        partsNum = new SimpleIntegerProperty(1);
        partSize = new SimpleIntegerProperty((int)file.length());
        cryptKey = null;
    }


    // Data Setters & Getters
    public String getAbsoluteFilename() { return file.getAbsolutePath(); }

    public void setDestPath(String path, boolean includeSubDir) {
        saveToSubDir = includeSubDir;
        destPath.set(path);
    }
    public String getDestPath() { return destPath.get(); }
    public String getFinalDestPath() {
        return destPath.get() + (saveToSubDir ? "\\"+file.getName().substring(0, file.getName().lastIndexOf('.'))+"_parts" : "");
    }
    public boolean saveToSubDir() { return saveToSubDir; }

    // splitMode
    public void setSplitMode(SplitMode mode) { splitMode.set(mode); }
    public SplitMode getSplitMode() { return splitMode.get(); }

    // partSize
    public void setPartSize(int size) {
        partSize.set(size);

        int pnum = (int)file.length() / partSize.get();
        if (((int)file.length() % partSize.get()) > 0)
            pnum++;
        partsNum.set(pnum);
    }
    public int getPartSize() { return partSize.get(); }

    // partsNum
    public void setPartsNum(int pnum) {
        partsNum.set(pnum);
        partSize.set((int)file.length() / pnum);
    }
    public int getPartsNum() { return partsNum.get(); }

    // cryptKey
    public void setCryptKey(String key) { this.cryptKey = key; }
    public String getCryptKey() { return this.cryptKey; }


    // Property Getters
    public StringProperty filenameProperty() { return new SimpleStringProperty(file.getName()); }
    public ObjectProperty<SplitMode> splitModeProperty() { return splitMode; }
    public IntegerProperty partsNumProperty() { return partsNum; }
    public IntegerProperty partSizeProperty() { return partSize; }
    public StringProperty destPathProperty() { return destPath; }



    /**
     * Returns a copy of this split options applied to a new file
     */
    public SplitFile getCopy(File file) {
        SplitFile copy = new SplitFile(file, splitMode.get());
        copy.setPartsNum(partsNum.get());
        copy.setDestPath(destPath.get(), saveToSubDir);
        copy.setCryptKey(cryptKey);
        return copy;
    }


    /**
     * Returns a SplitInfo object
     */
    public SplitInfo getSplitInfo(){
        SplitInfo obj = new SplitInfo(file, partsNum.get(), splitMode.get());

        //Calc key hash, if splitMode == CRYPTED
        if(splitMode.get()==SplitMode.CRYPTED) {
            obj.setKeyHash(Util.calcMD5(cryptKey.getBytes()));
        }

        return obj;
    }
}
