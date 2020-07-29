package filesplitterapp.model.splitter;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;


/**
 * L'interfaccia {@code Securable} viene utilizzata per racchiudere funzioni e costanti affini utilizzati sia da {@code CryptoSplitter} che da {@code CryptoMerger}.
 * <p>
 * Questa interfaccia prevede due costanti che definiscono l'algoritmo di cifratura ({@value #CIPHER_ALG}) e il rispettivo algoritmo usato<br>
 * per la chiave ({@value #KEY_ALG}).<br>
 * Inoltre sono presenti anche metodi per ottenere un oggetto {@code Cipher} inizializzato sia in cifratura che in decifratura.
 * <p>
 * Le funzioni contenute in questa interfaccia lanciano eccezioni di tipo {@code SecurableException}.
 *
 * @author Riccardo Rebottini
 */
public interface Securable {
    /** L'algoritmo di cifratura utilizzato */
    String CIPHER_ALG = "AES/CBC/PKCS5Padding";

    /**L'algoritmo usato per la chiave */
    String KEY_ALG = "AES";


    /**
     * Ritorna un {@code Cipher} inizializzato per la cifratura.
     * @param keyBytes chiave da utilizzare per la cifratura, in formato byte array.
     * @throws SecurableException in caso si verifichino errori in fase di inizializzazione del {@code Cipher}
     */
    default Cipher getCipher(byte[] keyBytes) throws SecurableException {
        return Private.getCipher(Cipher.ENCRYPT_MODE, keyBytes, null);
    }


    /**
     * Ritorna un {@code Cipher} inizializzato per la decifratura.
     * @param keyBytes chiave da utilizzare per la cifratura, in formato byte array.
     * @param params il parametro IV utilizzato dal {@code Cipher} in fase di cifratura, necessario per la modalità CBC
     * @throws SecurableException  in caso si verifichino errori in fase di inizializzazione del {@code Cipher}
     */
    default Cipher getDecipher(byte[] keyBytes, IvParameterSpec params) throws SecurableException {
        return Private.getCipher(Cipher.DECRYPT_MODE, keyBytes, params);
    }

    /**
     * Classe utilizzata per mantenere privato il metodo getCipher(opmode, keyBytes, params) generico, utilizzato sia da<br>
     * {@code getCipher} che da {@code getDecipher()}.
     */
    class Private {
        private static Cipher getCipher(int opmode, byte[] keyBytes, IvParameterSpec params) throws SecurableException {
            Cipher cipher;

            //Create a key
            Key key = new SecretKeySpec(keyBytes, KEY_ALG);

            //Create Cipher instance and initialize it to the specified opmode, with the specified params, if provided
            try {
                cipher = Cipher.getInstance(CIPHER_ALG);
                cipher.init(opmode, key, params);
                return cipher;
            }
            catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
                throw new SecurableException("Impossibile ottenere un'istanza di Cipher", ex);
            }
        }
    }
}
