package filesplitterapp.model.splitter;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CryptoSplitter extends Splitter implements Securable {
    private Cipher cipher;


    //TODO docs
    public CryptoSplitter(SplitInfo infos, String passwd) throws SplitterException {
        super(infos);

        try {
            passwd = calcMD5(passwd);
            info.setKeyHash(calcMD5(passwd));
            //System.out.println(("> Key Hash: "+info.getKeyHash()+" (len: "+info.getKeyHash().getBytes().length+")"));

            cipher = getCipher(Cipher.ENCRYPT_MODE, passwd.getBytes());
        } catch (SecurableException ex) {
            throw new SplitterException(ex);
        }

    }

    private boolean isLastPart(String pname) {
        return Integer.parseInt(String.valueOf(pname.charAt(pname.length()-1))) == info.getParts();
    }


    @Override
    protected void writeFile(File file, byte[] part) throws IOException {
        //System.out.print("> Writing crypted part " + file.getName());
        FileOutputStream fos = null;

        fos = new FileOutputStream(file);
        byte[] ciphered;
        try {
            if(isLastPart(file.getName())) {
                ciphered = cipher.doFinal(part);
                info.setIV(cipher.getIV());
            }
            else
                ciphered = cipher.update(part);

            //System.out.println(" (dim: "+ciphered.length+")");
            //System.out.println("> Content: "+ciphered);

            fos.write(ciphered);
            fos.close();
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            deleteParts();
            throw new IOException("Errore durante la cifratura del file\n"+file.getAbsolutePath(), ex);
        }
    }

}
