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

public class CryptoSplitter extends Splitter implements Securable {
    private byte[] keyBytes;
    private Cipher cipher = null;


    //TODO docs
    public CryptoSplitter(SplitInfo infos, String keyString) {
        super(infos);
        keyBytes = keyString.getBytes();
        info.setKeyHash(Securable.calcMD5(keyBytes));

        //Create a key
        if(keyBytes.length < KEY_LEN) keyBytes = Arrays.copyOf(keyBytes, KEY_LEN);
        Key key = new SecretKeySpec(keyBytes, KEY_ALG);

        // Create Cipher instance and initialize it to encryption mode
        try {
            cipher = Cipher.getInstance(CIPHER_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isLastPart(String pname) {
        int pnum = Integer.parseInt(String.valueOf(pname.charAt(pname.length() - 1)));
        if(pnum == info.getParts()) return true;
        return false;
    }


    @Override
    protected void writePart(byte[] part, File file) throws SplitterException {
        System.out.print("> Writing crypted part " + file.getName());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] ciphered;
            try {
                if(isLastPart(file.getName())) {
                    ciphered = cipher.doFinal(part);
                    info.setIV(cipher.getIV());
                }
                else
                    ciphered = cipher.update(part);

                System.out.println(" (dim: "+ciphered.length+")");
                System.out.println("> Content: "+ciphered);

                fos.write(ciphered);
                fos.close();
            } catch (BadPaddingException | IllegalBlockSizeException ex) {
                deleteParts();
                throw new SplitterException("Errore durante la cifratura del file\n"+file.getAbsolutePath()
                        +"\n\nFile "+info.getName()+" non diviso", ex);
            }
        }
        catch(IOException ex) {
            deleteParts();
            throw new SplitterException("Impossibile scrivere file\n"+file.getAbsolutePath()+"\n\nFile "+info.getName()+" non diviso", ex);
        }
    }

}
