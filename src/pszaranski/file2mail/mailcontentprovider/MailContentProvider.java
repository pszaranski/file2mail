package pszaranski.file2mail.mailcontentprovider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pszaranski.file2mail.Mediator;
import pszaranski.file2mail.configuration.Configuration;
import pszaranski.file2mail.exception.MailContentProviderException;

public class MailContentProvider {
	private static final Logger LOGGER = Logger.getLogger(MailContentProvider.class.getName());
	
	private Configuration config;
	private Mediator mediator;
	private String mailContent;

	public MailContentProvider(Mediator mediator) {
		this.mediator = mediator;
		this.config = mediator.getConfiguration();
		this.mailContent = "";
	}
	
	public void init() throws MailContentProviderException {
		LOGGER.log(Level.FINE, "Inicjalizacja modulu dostawcy tresci maili");
		
		File mailContentFile = new File(config.getSmtpMailContentFilePath());
		
		if (!mailContentFile.exists()) {
			throw new MailContentProviderException("Brak pliku z zawartoscia maila: " + mailContentFile.getAbsolutePath());
		}
		
		if (!mailContentFile.isFile()) {
			throw new MailContentProviderException("Podana sciezka nie prowadzi do pliku: " + mailContentFile.getAbsolutePath());
		}
		
		BufferedReader fr = null;
		try {
			fr = new BufferedReader(new FileReader(mailContentFile));
			String line = null;
			while ((line = fr.readLine()) != null) {
				this.mailContent += line;
			}
		} catch (FileNotFoundException e) {
			throw new MailContentProviderException("Brak pliku z zawartoscia maila: " + mailContentFile.getAbsolutePath());
		} catch (IOException e) {
			throw new MailContentProviderException("Blad odczytu pliku z zawartoscia maila: " + mailContentFile.getAbsolutePath(), e);
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Blad zamkniecia pliku z zawartoscia maila", e);
			}
		}
		
		this.mailContent = this.mailContent.trim();
		
		LOGGER.log(Level.INFO, "Inicjalizacja modulu dostawcy tresci maili zakonczona sukcesem");
	}
	
	public String getMailContent(List<File> attachments, List<String> recipients, Date date) throws MailContentProviderException {
		return mailContent.replaceAll("\\$\\{date\\}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
	}
}
