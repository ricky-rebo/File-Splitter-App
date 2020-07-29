package filesplitterapp.model.splitter;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;

public class CryptoMerger extends Merger implements Securable {
    private Cipher cipher;

    public CryptoMerger(SplitInfo info, String passwd) throws InvalidKeyException, SplitterException {
        super(info);

        try {
            passwd = calcMD5(passwd);

            //Inserted key check
            String keyHash = calcMD5(passwd);
            if(!keyHash.equals(info.getKeyHash()))
                throw new InvalidKeyException("Chiave inserita non valida");

            cipher = getCipher(Cipher.DECRYPT_MODE, passwd.getBytes(), new IvParameterSpec(info.getIV(keyHash)));
        } catch (SecurableException ex) {
            throw new SplitterException(ex);
        }
    }


    private boolean isLastPart(String pname) {
        int pnum = Integer.parseInt(String.valueOf(pname.charAt(pname.length() - 1)));
        return pnum == info.getParts();
    }


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
