package pszaranski.file2mail;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pszaranski.file2mail.archiver.Archiver;
import pszaranski.file2mail.configuration.Configuration;
import pszaranski.file2mail.dirscanner.DirScanner;
import pszaranski.file2mail.exception.ArchiverException;
import pszaranski.file2mail.exception.ConfigurationException;
import pszaranski.file2mail.exception.DirScannerException;
import pszaranski.file2mail.exception.MailContentProviderException;
import pszaranski.file2mail.exception.MailSenderException;
import pszaranski.file2mail.mailcontentprovider.MailContentProvider;
import pszaranski.file2mail.mailsender.MailSender;

public class Mediator {
	private static final Logger LOGGER = Logger.getLogger(Mediator.class.getName());
	
	private Configuration configuration;
	private DirScanner dirScanner;
	private MailSender mailSender;
	private Archiver archiver;
	private MailContentProvider mailContentProvider;

	private boolean init;
	
	public Mediator(String configFilePath) {
		this.configuration = new Configuration(configFilePath);
		
		this.dirScanner = new DirScanner(this);
		this.mailSender = new MailSender(this);
		this.archiver = new Archiver(this);
		this.mailContentProvider = new MailContentProvider(this);
	}
	
	public void init() throws ConfigurationException, DirScannerException, MailSenderException, ArchiverException, MailContentProviderException {
		LOGGER.log(Level.FINE, "Inicjalizacja modulu mediatora");
		this.configuration.init();
		
		this.dirScanner.init();
		this.mailSender.init();
		this.archiver.init();
		this.mailContentProvider.init();
		
		this.init = true;
		
		LOGGER.log(Level.INFO, "Inicjalizacja modulu mediatora zakonczona sukcesem");
	}
	
	public void sendMail(List<File> attachments) throws MailSenderException {
		this.mailSender.send(attachments);
	}
	
	public void archive(List<File> attachments) throws ArchiverException {
		this.archiver.archive(attachments);
	}
	
	public String getMailContent(List<File> attachments, List<String> recipients, Date date) throws MailContentProviderException {
		return this.mailContentProvider.getMailContent(attachments, recipients, date);
	}
	
	public void startApp() {
		this.dirScanner.start();
	}
	
	public void stopApp() {
		this.dirScanner.stop();
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public boolean isInit() {
		return init;
	}
}
