package filesplitterapp.model.splitter;

import java.io.*;

/**
 * Classe astratta da cui derivano gli {@code Splitter} e i {@code Merger}.
 * <p>
 * Definisce, tramite alcune costanti, l'estensione utilizzata per i file su cui sono salvate le parti, ({@value #PART_EXT}),
 * la dimensione del buffer utilizzato nella lettura del file ({@value #BUFFER_SIZE}).
 * <p>
 * Di per se questa classe non implementa proceddure di split/merge, ma definisce la lettura e scrittura base di un file,
 * tramite {@code readFile()} e {@code writeFile()}, che trattano i file in formato binario leggendo e scrivendo array di byte.
 * <p>
 * La classe definisce e implementa anche alcune classi "di utilità", tra cui:
 * - {@code getPartFile()} che ritorna il file della i-esima parte, il cui percorso
 *   assoluto è formato da {@code info.workspace + nome file originale + estensione} {@value #PART_EXT} {@code + i }<br>
 * - {@code deletParts()} che elimina tutte le parti già scritte.
 *
 * @author Riccardo Rebottini
 */
public abstract class FileManipulator {
    /** Estensione delle parti in cui un file viene diviso */
    protected static final String PART_EXT = ".par";

    /** Dimensione del buffer utilizzato per leggere un file */
    protected static final int BUFFER_SIZE = 4096;

    /** Contiene tutte le informazioni necessarie per il file da dividere/unire */
    protected SplitInfo info;


    /**
     * Costruttore che prende in input un oggetto {@code SplitInfo} e ne salva
     * il riferimento all'interno della classe.
     */
    public FileManipulator(SplitInfo info) {
        this.info = info;
    }

    /**
    * Ritorna il file contenente il percorso assoluto della i-esima parte, composto dal percorso
    * SplitInfo.workspace + il nome del file completo + l'estensione .par + il numero di parte (1~n)
    */
    protected File getPartFile(String dest, int i) {
        String filename = info.getName();
        return new File(dest,filename.substring(0, filename.lastIndexOf('.'))+PART_EXT+i);
    }


    /**
     * Cancella tutte le parti già scritte su disco. Appena raggiunge il
     * numero di parti o riscontra un file non esistente si interrompe.
     */
    public void deleteParts() {
        for(int i = 0; i<info.getPartsNum(); i++) {
            //Creo un riferimento all'i-esima parte
            File pfile = getPartFile(info.getWorkspace(), i+1);

            //Se esiste lo elimino, altrimenti ritorno
            if(pfile.exists() && pfile.isFile())
                pfile.delete();
            else return;
        }
    }


    /**
     * Legge un file e ne restituisce il contenuto in formato byte array.
     * <p>
     * La lettura del file non viene fatta in un solo passaggio, ma il suo contenuto viene letto a blocchi di {@value #BUFFER_SIZE}
     * bytes alla volta.
     *
    * @throws IOException in caso si verifichino errori durante la lettura del file.
    */
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


    /**
     * Scrive il contenuto di un byte array su un file.
     *
     * @throws IOException in caso si verifichino errori durante la scrittura del file.
    */
    protected void writeFile(File file, byte[] bytes) throws IOException {
        FileOutputStream fout;
        fout = new FileOutputStream(file);
        fout.write(bytes);
        fout.close();

    }
}
