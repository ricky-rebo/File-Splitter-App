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
    public CryptoSplitter(SplitInfo infos, String passwd) throws SecurableException {
        super(infos);
        passwd = calcMD5(passwd);
        info.setKeyHash(calcMD5(passwd));
        //System.out.println(("> Key Hash: "+info.getKeyHash()+" (len: "+info.getKeyHash().getBytes().length+")"));

        cipher = getCipher(Cipher.ENCRYPT_MODE, info.getKeyHash().getBytes());

    }

    private boolean isLastPart(String pname) {
        return Integer.parseInt(String.valueOf(pname.charAt(pname.length()-1))) == info.getParts();
    }


    @Override
    protected void writeFile(File file, byte[] part) throws SplitterException {
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
