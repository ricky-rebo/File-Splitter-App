package filesplitterapp.model.splitter;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CryptoMerger extends Merger implements Securable{
    private Cipher cipher;
    private byte[] keyBytes;

    public CryptoMerger(SplitInfo info, String key) throws InvalidKeyException {
        super(info);

        //Inserted key check
        String keyHash = Securable.calcMD5(key.getBytes());
        System.out.println("> Inserted key hash: "+keyHash+"\n> Original key hash: "+info.getKeyHash());
        if(!keyHash.equals(info.getKeyHash()))
            throw new InvalidKeyException("Chiave inserita non valida!");

        keyBytes = key.getBytes();
        if(keyBytes.length < KEY_LEN) keyBytes = Arrays.copyOf(keyBytes, KEY_LEN);
        System.out.println("> Merger keyBytes lenght: "+keyBytes.length);

        try {cipher = Cipher.getInstance(CIPHER_ALG);}
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex) { ex.printStackTrace(); }

        // Transformation of the algorithm
        try {cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, KEY_ALG), new IvParameterSpec(info.getIV()));}
        catch (InvalidKeyException | InvalidAlgorithmParameterException e1) { e1.printStackTrace(); }
    }


    private boolean isLastPart(String pname) {
        int pnum = Integer.parseInt(String.valueOf(pname.charAt(pname.length() - 1)));
        if(pnum == info.getParts()) return true;
        return false;
    }


    @Override
    protected byte[] readPart(File file) throws SplitterException {
        try {
            if(isLastPart(file.getName()))
                return cipher.doFinal(readFile(file));
            else
                return cipher.update(readFile(file));
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            throw new SplitterException("Impossibile decriptare file\n"+file.getAbsolutePath(), ex);
        }
    }
}
