package pszaranski.file2mail.archiver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pszaranski.file2mail.Mediator;
import pszaranski.file2mail.configuration.Configuration;
import pszaranski.file2mail.exception.ArchiverException;

public class Archiver {
	private static final Logger LOGGER = Logger.getLogger(Archiver.class.getName());
	private final Configuration config;
	private final Mediator mediator;
	private File archiveDir;
	
	public Archiver(Mediator mediator) {
		this.mediator = mediator;
		this.config = mediator.getConfiguration();
	}
	
	public void init() throws ArchiverException {
		LOGGER.log(Level.FINE, "Inicjalizacja modulu archiwizacji");
		
		this.archiveDir = new File(config.getArchiveDirPath());
		
		if (!archiveDir.exists() && config.isArchiveDirCreateOnStartup()) {
			if (!this.archiveDir.mkdir()) {
				throw new ArchiverException("Blad podczas tworzenia katalogu archiwum: " + config.getArchiveDirPath());
			}
		} else if (!archiveDir.exists()) {
			throw new ArchiverException("Brak katalogu archiwum: " + config.getArchiveDirPath());
		}
		
		LOGGER.log(Level.INFO, "Inicjalizacja modulu archiwizacji zakonczona sukcesem");
	}
	
	public void archive(List<File> attachments) throws ArchiverException {
		for (File attachment: attachments) {
			String[] fileNameParts = attachment.getName().split("\\.");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
				
			String destFileName = "";
				
			for (int i = 0; i < fileNameParts.length; i++) {
				destFileName += fileNameParts[i];
					
				if (i == fileNameParts.length - 2) {
					destFileName += "_" + sdf.format(new Date()) + ".";
				}
			}
				
			File destFile = new File(archiveDir, destFileName);
			
			
			if (!attachment.renameTo(destFile)) {
				LOGGER.log(Level.SEVERE, "Blad podczas przenoszenia pliku do archiwum: " + attachment.getName());
				throw new ArchiverException("Blad podczas przenoszenia pliku do archiwum: " + attachment.getName());
			}
			
			LOGGER.log(Level.INFO, "Zalacznik: " + attachment.getName() + " zostal przeniesiony do archiwum jako: " + destFile.getName());
		}
	}
}
