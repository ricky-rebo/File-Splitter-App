package filesplitterapp.model.splitter;

import javax.xml.bind.DatatypeConverter;
import java.security.*;

public interface Securable {
    String CIPHER_ALG = "AES/CBC/PKCS5Padding";
    String KEY_ALG = "AES";
    String HASH_ALG = "MD5";
    int KEY_LEN = 32;

    static String calcMD5(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(HASH_ALG);
            md.update(bytes);
            return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
