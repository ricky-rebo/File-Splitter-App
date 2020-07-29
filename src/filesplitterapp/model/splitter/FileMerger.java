package filesplitterapp.model.splitter;

import filesplitterapp.util.Util;
import filesplitterapp.model.splitter.Splitter.SplitMode;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

/**
 * TODO docs
 * @author Riccardo Rebottini
 */
public class FileMerger extends FileDimModifier {
	private byte[] keyBytes = null;
	private Cipher cipher;

	//TODO docs
	public FileMerger(SplitInfo info, String key) throws InvalidKeyException  {
		super(info);

		if(info.getSplitMode() == SplitMode.CRYPTO) {
			//Inserted key check
			String keyHash = Util.calcMD5(key.getBytes());
			if(!keyHash.equals(info.getKeyHash()))
				throw new InvalidKeyException("Chiave inserita non valida!");

			keyBytes = key.getBytes();
			if(keyBytes.length < 32) keyBytes = Arrays.copyOf(keyBytes, 32);
			System.out.println("> Merger keyBytes lenght: "+keyBytes.length);

			try {cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");}
			catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {ex.printStackTrace();}

			// Transformation of the algorithm
			//TODO add key check
			try {cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));}
			catch (InvalidKeyException e1) {e1.printStackTrace();}
		}
	}

	//TODO docs
	public void merge(String saveTo) throws Exception {
		FileOutputStream fout = null;
		byte[] fileBytes = null;

		//Check if a file with the same name already exist in that location
		String mergedFilename;
		if(info.getFile().exists() && info.getFile().isFile())
			mergedFilename = saveTo+SEPARATOR+"merged_"+info.getName();
		else
			mergedFilename = saveTo+SEPARATOR+info.getName();

		//TODO re-do merge procedure
		//	write a function that get a part based on SplitMode
		//	and slim the for cycle here
		try {
			fout = new FileOutputStream(mergedFilename);
			String partLocation = info.getFile().getParent()+SEPARATOR;
			for(int i = 0; i<info.getParts(); i++) {
				switch(info.getSplitMode()) {
					case DEFAULT:
						fileBytes = readFile(getPartFile(partLocation, i+1));
						break;
					case ZIP:
						fileBytes = unzipPart(getPartFile(partLocation, i+1));
						break;
					case CRYPTO:
						fileBytes = uncryptPart(getPartFile(partLocation, i+1));
				}
				if(fileBytes == null) throw new Exception("Impossibile leggere file " + getPartFile(partLocation, i+1).getName());
				fout.write(fileBytes);
				//TODO remove debug output
				//System.out.println("> loaded part "+(i+1)+" of "+info.getParts());
			}
			fout.close();
		}
		catch(IOException e) {e.printStackTrace();}
	}


	private byte[] unzipPart(File file) {
		ByteArrayOutputStream baos = null;
		byte[] buffer = null;
		ZipInputStream zis = null;

		try {
			baos = new ByteArrayOutputStream();
			zis = new ZipInputStream(new FileInputStream(file));
			buffer = new byte[1024];
			int len;
			while((len = zis.read(buffer))>0)
				baos.write(buffer, 0, len);
			zis.closeEntry();
			zis.close();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}

		return baos.toByteArray();
	}


	private byte[] uncryptPart(File file) {
        return cipher.update(readFile(file));
	}


	//TODO docs
	public void deletePartFiles() {
		new File(info.getFile().getParent()+SEPARATOR+info.getInfoFilename()).delete();
		for(int i=0; i<info.getParts(); i++) {
			getPartFile(info.getFile().getParent()+SEPARATOR, i+1).delete();
		}
	}
}