package filesplitterapp.model.splitter;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.*;

public interface Securable {
    String CIPHER_ALG = "AES/CBC/PKCS5Padding";
    String KEY_ALG = "AES";
    String HASH_ALG = "MD5";

    default String calcMD5(String text) throws SecurableException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(HASH_ALG);
            md.update(text.getBytes());
            return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new SecurableException(ex);
        }
    }

    default Cipher getCipher(int opmode, byte[] keyBytes) throws SecurableException {
        return getCipher(opmode, keyBytes, null);
    }

    default Cipher getCipher(int opmode, byte[] keyBytes, IvParameterSpec params) throws SecurableException {
        Cipher cipher;

        //Create a key
        Key key = new SecretKeySpec(keyBytes, KEY_ALG);

        //Create Cipher instance and initialize it to the specified opmode, with the specified params, if provided
        try {
            cipher = Cipher.getInstance(CIPHER_ALG);
            if(params == null) cipher.init(opmode, key);
            else cipher.init(opmode, key, params);
            return cipher;
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
            throw new SecurableException(ex);
        }
    }
}
