package filesplitter.model.splitter;

import filesplitter.util.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * TODO docs
 * @author Riccardo Rebottini
 */
public class FileMerger extends FileDimModifier {
	private byte[] keyBytes = null;

	//TODO docs
	public FileMerger(SplitInfo info, String key) throws InvalidKeyException  {
		super(info);

		if(info.getSplitMode() == SplitMode.CRYPTED) {
			//Inserted key check
			String keyHash = Util.calcMD5(key.getBytes());
			if(!keyHash.equals(info.getKeyHash()))
				throw new InvalidKeyException("Chiave inserita non valida!");

			keyBytes = key.getBytes();
			if(keyBytes.length < 32) keyBytes = Arrays.copyOf(keyBytes, 32);
		}
	}

	//TODO docs
	public void merge(String saveTo){
		FileOutputStream fout = null;
		byte[] fileBytes = null;

		//Check if a file with the same name already exist in that location
		String mergedFilename;
		if(info.getFile().exists() && info.getFile().isFile())
			mergedFilename = saveTo+"\\merged_"+info.getName();
		else
			mergedFilename = saveTo+'\\'+info.getName();

		//TODO re-do merge procedure
		//	write a function that get a part based on SplitMode
		//	and slim the for cycle here
		try {
			fout = new FileOutputStream(mergedFilename);
			String partLocation = info.getFile().getParent()+'\\';
			for(int i = 0; i<info.getParts(); i++) {
				switch(info.getSplitMode()) {
					case DEFAULT:
						fileBytes = readFile(getPartFile(partLocation, i+1));
						break;
					case ZIP:
						fileBytes = unzipPart(getPartFile(partLocation, i+1));
						break;
					case CRYPTED:
						fileBytes = uncryptPart(getPartFile(partLocation, i+1));
				}
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
			//System.out.print("> Loading zip part "+file.toString()+" (size: "+file.length()+")");
			ZipEntry zipEntry = zis.getNextEntry();

			//System.out.println(zipEntry.getSize());

			buffer = new byte[1024];
			int len;
			while((len = zis.read(buffer))>0)
				baos.write(buffer, 0, len);
			zis.closeEntry();
			zis.close();
		}
		catch(IOException ex) {ex.printStackTrace();}

		//System.out.println(" (unzipped size: "+baos.size()+")");
		return baos.toByteArray();
	}


	private byte[] uncryptPart(File file) {
		// Create Cipher instance and initialize it to encryption mode
        Cipher cipher = null;
		try {cipher = Cipher.getInstance("AES");}
		catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {ex.printStackTrace();}

		// Transformation of the algorithm
		//TODO add key check
        try {cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));}
        catch (InvalidKeyException e1) {e1.printStackTrace();}

        try {return cipher.doFinal(readFile(file));}
        catch (IllegalBlockSizeException | BadPaddingException e) {e.printStackTrace();}

        return null;
	}


	//TODO docs
	public void deletePartFiles() {
		new File(info.getFile().getParent()+'\\'+info.getInfoFilename()).delete();
		for(int i=0; i<info.getParts(); i++) {
			getPartFile(info.getFile().getParent()+'\\', i+1).delete();
		}
	}
}