package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class FileUtils.
 *
 * @author vincenzoingenito
 *
 * Utility to manager file.
 */
@Slf4j
public final class FileUtility {
 
	/**
	 * Max size chunk.
	 */
	private static final int CHUNK_SIZE = 16384;

	/**
	 * Constructor.
	 */
	private FileUtility() {
	}


	/**
	 * Method to get the file's content from fs.
	 *
	 * @param filename	filename
	 * @return			content
	 */
	public static byte[] getFileFromFS(final String filename) {
		byte[] b = null;
		try {
			File f = new File(filename);
			InputStream is = new FileInputStream(f);
			b = getByteFromInputStream(is);
			is.close();
		} catch (Exception e) {
			log.error("FILE UTILS getFileFromFS(): Errore in fase di recupero del contenuto di un file da file system. ", e);
		}
		return b;
	}

	/**
	 * Metodo per il recupero del contenuto di un file dalla folder interna "/src/main/resources".
	 *
	 * @param filename	nome del file
	 * @return			contenuto del file
	 */
	public static byte[] getFileFromInternalResources(final String filename) {
		byte[] b = null;
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
			b = getByteFromInputStream(is);
			is.close();
		} catch (Exception e) {
			log.error("FILE UTILS getFileFromInternalResources(): Errore in fase di recupero del contenuto di un file dalla folder '/src/main/resources'. ", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error(""+e);
				}
			}
		}
		return b;
	}

	/**
	 * Recupero contenuto file da input stream.
	 *
	 * @param is
	 *            input stream
	 * @return contenuto file
	 */
	private static byte[] getByteFromInputStream(final InputStream is) {
		byte[] b;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[CHUNK_SIZE];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			b = buffer.toByteArray();
		} catch (Exception e) {
			log.error("Errore durante il trasform da InputStream a byte[]: ", e);
			throw new BusinessException(e);
		}
		return b;
	}

}
