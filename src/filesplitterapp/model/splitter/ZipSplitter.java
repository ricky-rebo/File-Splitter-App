package filesplitterapp.model.splitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * {@code ZipSplitter} estende {@code Splitter}, implementando la scrittura di parti in formato compresso.
 * <p>
 * Questa funzionalità si ottiene facendo un override di {@code writeFile()} di {@code FileManipulator}. In questo modo la procedura <br>
 * di split definita in {@code Splitter} non cambia, ma viene modificato solo il modo in cui vengono scritte le parti.
 *
 * @author Riccardo Rebottini
 */
public class ZipSplitter extends Splitter {
    /**
     * Cre un nuovo oggetto {@code ZipSplitter}
     *
     * @param infos lo {@code SplitInfo} contenente il file da dividere e le relative impostazioni di divisione
     */
    public ZipSplitter(SplitInfo infos) {
        super(infos);
    }


    /**
     * Scrive il contenuto di un byte array su un file compresso in un archivio zip.
     *
     * @throws IOException in caso si verifichino problemi durante la scrittura del file
     */
    @Override
    protected void writeFile(File file, byte[] bytes) throws IOException {
        ZipOutputStream zos;

        zos = new ZipOutputStream(new FileOutputStream(file));
        zos.putNextEntry(new ZipEntry(file.getName()));
        zos.write(bytes);
        zos.closeEntry();
        zos.close();

    }
}
