package filesplitterapp.model.splitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

/**
 * {@code ZipMerger} estende {@code Merger}, implementando la lettura di parti in formato compresso.
 * <p>
 * Questa funzionalità si ottiene facendo un override di {@code readFile()} di {@code FileManipulator}. In questo modo la procedura <br>
 * di merge definita in {@code Merger} non cambia, ma viene modificato solo il modo in cui vengono lette le parti.
 *
 * @author Riccardo Rebottini
 */
public class ZipMerger extends Merger {

    /**
     * Crea un nuovo oggetto {@code ZipMerger}.
     *
     * @param info lo {@code SplitInfo} contenente il file da unire e le relative impostazioni con cui è stato diviso
     */
    public ZipMerger(SplitInfo info) {
        super(info);
    }

    /**
     * Legge un file compresso e ne ritorna il contenuto in formato byte array.
     * <p>
     * La lettura non avviene in un passaggio unico, ma il contenuto del file viene letto a blocchi di
     * {@value #BUFFER_SIZE} bytes alla volta.
     *
     * @throws IOException in caso si verifichino problemi durante la lettura del file
     */
    @Override
    protected byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        ZipInputStream zis;

        zis = new ZipInputStream(new FileInputStream(file));
        zis.getNextEntry();

        while((len=zis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }

        zis.closeEntry();
        zis.close();

        return baos.toByteArray();
    }
}
