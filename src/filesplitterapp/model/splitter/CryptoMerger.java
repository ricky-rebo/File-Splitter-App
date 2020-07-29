package filesplitterapp.model.splitter;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * {@code CryptoMerger} estende {@code Merger}, implementando la lettura di parti in formato crittografato.
 * <p>
 * Questa funzionalità si ottiene facendo un override di {@code readFile()} di {@code FileManipulator}. In questo modo la procedura <br>
 * di merge definita in {@code Merger} non cambia, ma viene modificato solo il modo in cui vengono lette le parti.
 *
 * @author Riccardo Rebottini
 */
public class CryptoMerger extends Merger implements Securable {
    private Cipher cipher;

    /**
     * Crea un nuovo oggetto {@code CryptoMerger}.
     * <p>
     * Il costruttore salva le informazioni sul file da unire, verificha la chiave fornita, e se è corretta
     * inizializza il {@code Cipher}.
     *
     * @throws InvalidKeyException in caso la chiave fornita non sia corretta
     * @throws SplitterException in caso si verifichino degli errori durante l'inizializzazione del {@code Cipher}
     */
    public CryptoMerger(SplitInfo info, String passwd) throws InvalidKeyException, SplitterException {
        super(info);

        try {
            cipher = getDecipher(info.verifyKey(passwd.getBytes()), new IvParameterSpec(info.getIV()));
        }
        catch (SecurableException ex) {
            throw new SplitterException("Impossibile unire il file "+info.getName(), ex);
        }
    }

    //Ritorna true se il nome del file assato corrisponde al nome dell'ultima parte
    //Utilizzato da writeFile() per decidere se usare cipher.update() o cipher.doFinal()
    private boolean isLastPart(String pname) {
        int pnum = Integer.parseInt(pname.substring(pname.lastIndexOf('.')+4));
        return pnum == info.getPartsNum();
    }


    /**
     * Legge il contenuto di un file crittografato, e ritorna il contenuto in formato byte array.
     *
     * @throws IOException in cas si verifichino problemi durante la lettura del file o la decifratura del contenuto
     */
    @Override
    protected byte[] readFile(File file) throws IOException {
        try {
            if(isLastPart(file.getName()))
                return cipher.doFinal(super.readFile(file));
            else
                return cipher.update(super.readFile(file));
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            throw new IOException("Impossibile decriptare file\n"+file.getAbsolutePath(), ex);
        }
    }
}
