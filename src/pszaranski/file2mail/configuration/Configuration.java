package pszaranski.file2mail.configuration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import pszaranski.file2mail.exception.ConfigurationException;

public class Configuration {
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
	
	private static final String WORK_DIR_SCAN_PATH_PROPERTY = "workDir.scan.path";
	private static final String WORK_DIR_SCAN_INTERVAL_PROPERTY = "workDir.scan.interval";
	private static final String WORK_DIR_SCAN_SINGLE_FILE_INTERVAL = "workDir.scan.singleFileInterval";
	private static final String WORK_DIR_CREATE_ON_STARTUP_PROPERTY = "workDir.createOnStartup";
	private static final String WORK_DIR_SCAN_LOCK_FILES = "workDir.scan.lockFiles";
	
	private static final String SMTP_ACCOUNT_USERNAME_PROPERTY = "smtp.account.username";
	private static final String SMTP_ACCOUNT_PASSWORD_PROPERTY = "smtp.account.password";
	
	private static final String SMTP_MAIL_RECIPIENTS_PROPERTY = "smtp.mail.recipients";
	private static final String SMTP_MAIL_AUTHOR_PROPERTY = "smtp.mail.author";
	private static final String SMTP_MAIL_SUBJECT_PROPERTY = "smtp.mail.subject";
	
	private static final String SMTP_SERVER_HOST_PROPERTY = "smtp.server.host";
	private static final String SMTP_SERVER_PORT_PROPERTY = "smtp.server.port";
	private static final String SMTP_SERVER_REQUIRES_SSL_PROPERTY = "smtp.server.requiresSSL";
	
	private static final String ARCHIVE_DIR_PATH_PROPERTY = "archiveDir.path";
	private static final String ARCHIVE_DIR_CREATE_ON_STARTUP_PROPERTY = "archiveDir.createOnStartup";
	private static final String SMTP_MAIL_CONTENT_FILE_PATH = "smtp.mail.contentFilePath";
	private static final String SMTP_MAIL_GROUP_ATACHMENTS =  "smtp.mail.groupAttachments";
	private static final String SMTP_MAIL_GROUP_ATTACHMENTS_MAX_SIZE = "smtp.mail.groupAttachmentsMaxSize";
	
	private static final String SMTP_MAIL_GROUP_RECIPIENTS = "smtp.mail.groupRecipients";
			
	private Properties props;
	private final String configFilePath;
	
	private String workDirScanPath;
	private long workDirScanInterval;
	private long workDirScanSingleFileInterval;
	private boolean workDirScanLockFiles;
	private boolean workDirCreateOnStartup;
	private String smtpAccountUsername;
	private String smtpAccountPassword;
	private String smtpMailAuthor;
	private String smtpMailRecipients;
	private String smtpMailSubject;
	private String smtpServerHost;
	private int smtpServerPort;
	private boolean smtpServerRequiresSSL;
	private String archiveDirPath;
	private boolean archiveDirCreateOnStartup;
	private String smtpMailContentFilePath;
	private boolean smtpMailGroupAttachments;
	private long smtpMailGroupAttachmentsMaxSize;
	private boolean smtpMailGroupRecipients;
	
	public Configuration(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public void init() throws ConfigurationException {
		LOGGER.log(Level.FINE, "Inicjalizacja modulu konfiguracji");
		
		this.props = new Properties();
		
		File configFile = new File(this.configFilePath);
		
		if (this.configFilePath == null || this.configFilePath.isEmpty()) {
			throw new ConfigurationException("Brak sciezki do pliku konfiguracujnego");
		}
		
		FileInputStream configFileInputStream = null;
		
		try {
			configFileInputStream = new FileInputStream(configFile);
			this.props.load(configFileInputStream);
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Brak pliku konfiguracyjnego pod podana sciezka: " + configFile.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new ConfigurationException("Blad odczytu pliku konfiguracyjnego: " + this.configFilePath, e);
		} finally {
			if (configFileInputStream != null) {
				try {
					configFileInputStream.close();
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Blad zamkniecia pliku konfiguracyjnego: " + configFilePath, e);
				}
			}
		}
		
		// TODO weryfikacja konfiguracji
		this.workDirScanPath = props.getProperty(WORK_DIR_SCAN_PATH_PROPERTY);
		this.workDirScanInterval = Long.parseLong(props.getProperty(WORK_DIR_SCAN_INTERVAL_PROPERTY));
		this.workDirScanSingleFileInterval = Long.parseLong(props.getProperty(WORK_DIR_SCAN_SINGLE_FILE_INTERVAL));
		this.workDirCreateOnStartup = Boolean.parseBoolean(props.getProperty(WORK_DIR_CREATE_ON_STARTUP_PROPERTY));
		this.workDirScanLockFiles = Boolean.parseBoolean(props.getProperty(WORK_DIR_SCAN_LOCK_FILES));
		
		this.smtpAccountUsername = props.getProperty(SMTP_ACCOUNT_USERNAME_PROPERTY);
		this.smtpAccountPassword = props.getProperty(SMTP_ACCOUNT_PASSWORD_PROPERTY);
		this.smtpMailAuthor = props.getProperty(SMTP_MAIL_AUTHOR_PROPERTY);
		// TODO szczegolowa weryfikacja adresu mailowego
		this.smtpMailRecipients = props.getProperty(SMTP_MAIL_RECIPIENTS_PROPERTY);
		this.smtpMailSubject = props.getProperty(SMTP_MAIL_SUBJECT_PROPERTY);
		this.smtpServerHost = props.getProperty(SMTP_SERVER_HOST_PROPERTY);
		this.smtpServerPort = Integer.parseInt(props.getProperty(SMTP_SERVER_PORT_PROPERTY));
		this.smtpServerRequiresSSL = Boolean.parseBoolean(props.getProperty(SMTP_SERVER_REQUIRES_SSL_PROPERTY));
		this.archiveDirPath = props.getProperty(ARCHIVE_DIR_PATH_PROPERTY);
		this.archiveDirCreateOnStartup = Boolean.parseBoolean(props.getProperty(ARCHIVE_DIR_CREATE_ON_STARTUP_PROPERTY));
		this.smtpMailContentFilePath = props.getProperty(SMTP_MAIL_CONTENT_FILE_PATH);
		
		this.smtpMailGroupAttachments = Boolean.parseBoolean(props.getProperty(SMTP_MAIL_GROUP_ATACHMENTS));
		this.smtpMailGroupAttachmentsMaxSize = Long.parseLong(props.getProperty(SMTP_MAIL_GROUP_ATTACHMENTS_MAX_SIZE));
		this.smtpMailGroupRecipients = Boolean.parseBoolean(props.getProperty(SMTP_MAIL_GROUP_RECIPIENTS));
		
		LOGGER.info("Konfiguracja\r\n" + printProperties(this.props));
		LOGGER.log(Level.INFO, "Inicjalizacja modulu konfiguracji zakonczona sukcesem");
	}
	
	
	public String getWorkDirPath() {
		return workDirScanPath;
	}
	
	public Long getWorkDirScanInterval() {
		return workDirScanInterval;
	}
	
	public long getWorkDirScanSingleFileInterval() {
		return workDirScanSingleFileInterval;
	}
	
	public boolean isWorkDirScanLockFiles() {
		return workDirScanLockFiles;
	}
	
	public Boolean isWorkDirCreateOnStartup() {
		return workDirCreateOnStartup;
	}
	
	public String getSmtpAccountUsername() {
		return smtpAccountUsername;
	}
	
	public String getSmtpAccountPassword() {
		return smtpAccountPassword;
	}
	
	public String getSmtpMailAuthor() {
		return smtpMailAuthor;
	}
	
	public String getSmtpMailRecipients() {
		return smtpMailRecipients;
	}
	
	public String getSmtpMailSubject() {
		return smtpMailSubject;
	}
	
	public String getSmtpServerHost() {
		return smtpServerHost;
	}
	
	public Integer getSmtpServerPort() {
		return smtpServerPort;
	}
	
	public Boolean isSmtpServerRequiresSSL() {
		return smtpServerRequiresSSL;
	}
	
	public String getArchiveDirPath() {
		return archiveDirPath;
	}
	
	public Boolean isArchiveDirCreateOnStartup() {
		return archiveDirCreateOnStartup;
	}
	
	public String getSmtpMailContentFilePath() {
		return smtpMailContentFilePath;
	}
	
	public long getSmtpMailGroupAttachmentsMaxSize() {
		return smtpMailGroupAttachmentsMaxSize;
	}
	
	public boolean isSmtpMailGroupAtachments() {
		return smtpMailGroupAttachments;
	}
	
	public boolean isSmtpMailGroupRecipients() {
		return smtpMailGroupRecipients;
	}
	
	private String printProperties(Properties props) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		props.list(new PrintStream(out));
		
		return new String(out.toByteArray());
	}
}
