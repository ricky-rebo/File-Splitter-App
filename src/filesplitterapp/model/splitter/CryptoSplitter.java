package filesplitterapp.model.splitter;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * {@code CryptoSplitter} estende {@code Splitter}, implementando la scrittura di file in formato crittografato.
 * <p>
 * Questa funzionalità si ottiene facendo un override di {@code writeFile()} di {@code FileManipulator}. In questo modo la procedura <br>
 * di split definita in {@code Splitter} non cambia, ma viene modificato solo il modo in cui vengono scritte le parti.
 *
 * @author Riccardo Rebottini
 */
public class CryptoSplitter extends Splitter implements Securable {
    private Cipher cipher;


    /**
     * Crea un nuovo oggetto {@code CryptoSplitter}.
     * <p>
     * Il costruttore salva le informazioni sul file da dividere, e inizializza il {@code Cipher} con la chiave fornita.
     *
     * @throws SplitterException in caso si verifichino problemi durante l'inizializzazione del {@code Cipher}
     */
    public CryptoSplitter(SplitInfo infos, String passwd) throws SplitterException {
        super(infos);
        info.setKeyHash(passwd.getBytes());

        try {
            cipher = getCipher(passwd.getBytes());
        } catch (SecurableException ex) {
            throw new SplitterException("Impossibile dividere il file "+info.getFile().getAbsolutePath(), ex);
        }

    }

    //Ritorna true se il nome del file assato corrisponde al nome dell'ultima parte
    //Utilizzato da writeFile() per decidere se usare cipher.update() o cipher.doFinal()
    private boolean isLastPart(String pname) {
        int pnum = Integer.parseInt(pname.substring(pname.lastIndexOf('.')+4));
        return pnum == info.getPartsNum();
    }


    /**
     * Scrive un byte array in formato crittografato su un file.
     *
     * @throws IOException in caso si verifichino problemi durante la cifratura del contenuto o la scrittura del file
     */
    @Override
    protected void writeFile(File file, byte[] part) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(file);
        byte[] ciphered;
        try {
            if(isLastPart(file.getName())) {
                ciphered = cipher.doFinal(part);
                info.setIV(cipher.getIV());
            }
            else
                ciphered = cipher.update(part);

            fos.write(ciphered);
            fos.close();
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            deleteParts();
            throw new IOException("Errore durante la cifratura del file\n"+file.getAbsolutePath(), ex);
        }
    }

}
