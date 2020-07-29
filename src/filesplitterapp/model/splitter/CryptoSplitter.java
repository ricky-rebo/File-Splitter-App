package filesplitterapp.model.splitter;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CryptoSplitter extends Splitter {
    private byte[] keyBytes;
    private Cipher cipher = null;


    //TODO docs
    public CryptoSplitter(SplitInfo infos, String keyString) {
        super(infos);
        keyBytes = keyString.getBytes();

        //Create a key
        if(keyBytes.length < 32) keyBytes = Arrays.copyOf(keyBytes, 32);
        Key key = new SecretKeySpec(keyBytes, "AES");

        // Create Cipher instance and initialize it to encryption mode
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void writePart(byte[] part, File file) {
        System.out.print("> Writing crypted part " + file.getName());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] ciphered = cipher.update(part);
            System.out.println(" (dim: "+ciphered.length+")");
            System.out.println("> Content: "+ciphered);
            fos.write(ciphered);
            fos.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}
