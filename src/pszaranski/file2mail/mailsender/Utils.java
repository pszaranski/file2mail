package pszaranski.file2mail.mailsender;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
	private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
	
	private static final String[] SIZES = new String[]{"B", "KB", "MB", "GB", "TB", "PT", "EB", "ZB", "YB", "BB", "GeB"};
	
	public static String formatSize(long size) {
		StringBuilder sb = new StringBuilder();
		LOGGER.log(Level.FINEST, "Formatuje rozmiar: " + size);

		int i = 0;
		for (; i < SIZES.length && size > 1023; i++) {
			size = size / 1024;
		}
		
		sb.append(size).append(" ").append(SIZES[i]);
		
		String formattedSize = sb.toString();
		
		LOGGER.log(Level.FINEST, "Sformatowany rozmiar: " + formattedSize + " wynosi: " + formattedSize);
		
		return formattedSize;
	}
}
