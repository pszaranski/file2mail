package pszaranski.file2mail.mailsender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

import pszaranski.file2mail.Mediator;
import pszaranski.file2mail.configuration.Configuration;
import pszaranski.file2mail.exception.ArchiverException;
import pszaranski.file2mail.exception.MailContentProviderException;
import pszaranski.file2mail.exception.MailSenderException;

public class MailSender {
	private static final String MAIL_SMTP_STARTTLS_ENABLE_PROPERTY = "mail.smtp.starttls.enable";
	private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
	private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
	
	private static final Logger LOGGER = Logger.getLogger(MailSender.class.getName());

	private final Configuration config;
	private final Mediator mediator;
	
	public MailSender(Mediator mediator) {
		this.mediator = mediator;
		this.config = mediator.getConfiguration();
	}
	
	public void init() throws MailSenderException {
		LOGGER.log(Level.FINE, "Inicjalizacja modulu wysylacza maili");
		
		LOGGER.log(Level.INFO, "Test polaczenia SMTP");
		Session session = getSession();
		Transport transport = null;
		
		try {
			transport = session.getTransport("smtp");
			transport.connect(config.getSmtpAccountUsername(), config.getSmtpAccountPassword());
		} catch (NoSuchProviderException e) {
			throw new MailSenderException("Blad podczas testu polaczenia", e);
		} catch (MessagingException e) {
			throw new MailSenderException("Blad podczas testu polaczenia", e);
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					LOGGER.log(Level.WARNING, "Blad zamkniecia polaczenia z serwerem SMTP", e);
				}
			}
		}
		LOGGER.log(Level.INFO, "Test polaczenia SMTP zakonczony sukcesem");
		
		LOGGER.log(Level.INFO, "Inicjalizacja modulu wysylacza maili zakonczona sukcesem");
	}
	
	public void send(List<File> attachments) throws MailSenderException {
		LOGGER.log(Level.INFO, "Rozpoczeto nowe zadanie");
		Session session = getSession();
		
		Transport transport = null;
		
		try {
			transport = session.getTransport("smtp");
			transport.connect(config.getSmtpAccountUsername(), config.getSmtpAccountPassword());
			
			if (config.isSmtpMailGroupAtachments()) {
				if (config.isSmtpMailGroupRecipients()) {
					List<String> recipients = Arrays.asList(config.getSmtpMailRecipients().split(";"));
					
					innerSendMessage(session, attachments, transport, recipients, true);
				} else {
					for (String recipient: config.getSmtpMailRecipients().split(";")) {
						innerSendMessage(session, attachments, transport, Arrays.asList(recipient), false);
					}
					mediator.archive(attachments);
				}
			} else {
				if (config.isSmtpMailGroupRecipients()) {
					for (File attachment: attachments) {
						List<String> recipients = Arrays.asList(config.getSmtpMailRecipients().split(";"));
						
						innerSendMessage(session, Arrays.asList(attachment), transport, recipients, true);
					}
				} else {
					for (File attachment: attachments) {
						for (String recipient: config.getSmtpMailRecipients().split(";")) {
							innerSendMessage(session, Arrays.asList(attachment), transport, Arrays.asList(recipient), false);
						}
						mediator.archive(Arrays.asList(attachment));
					}
				}
			}
		} catch (AddressException e) {
			throw new MailSenderException("Nieprawidlowy adres zrodlowy (" + config.getSmtpMailAuthor() + ") lub docelowy (" + config.getSmtpMailRecipients() + ") maila.", e);
		} catch (MessagingException e) {
			throw new MailSenderException("Blad konstrukcji wiadomosci email", e);
		} catch (MailContentProviderException e) {
			throw new MailSenderException("Blad podczas dostarczania tresci maila", e);
		} catch (ArchiverException e) {
			throw new MailSenderException("Blad podczas archiwizacji danych", e);
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					LOGGER.log(Level.WARNING, "Blad zamykania polaczenia z serwerem SMTP", e);
				}
			}
		}
		
		LOGGER.log(Level.INFO, "Zadanie zakonczone powodzeniem");
	}
	
	private MimeMessage createMessage(Session session, List<File> attachments, List<String> recipients) throws AddressException, MessagingException, MailContentProviderException {
		MimeMessage message = new MimeMessage(session);
		
		message.setFrom(new InternetAddress(config.getSmtpMailAuthor()));
		message.setSubject(config.getSmtpMailSubject());
		BodyPart messageBodyBart = new MimeBodyPart();
		messageBodyBart.setContent(mediator.getMailContent(attachments, recipients, new Date()), "text/html");
		
		Multipart multiPart = new MimeMultipart();
		multiPart.addBodyPart(messageBodyBart);
		
		for (File attachment: attachments) {
			BodyPart attachmentBodyPart = new MimeBodyPart();
			attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(attachment)));
			attachmentBodyPart.setFileName(attachment.getName());
			
			multiPart.addBodyPart(attachmentBodyPart);
		}
		
		message.setContent(multiPart);
		
		return message;
	}
	
	private void innerSendMessage(Session session, List<File> attachments, Transport transport, List<String> recipients, boolean archiveAttachments) throws MailSenderException, AddressException, MessagingException, MailContentProviderException, ArchiverException {
		LOGGER.info("Zadanie wyslania maila do: " + recipients +", z zalacznikami: " + attachments);
		long messageSize = 0;
		List<File> addedAttachments = new ArrayList<File>();
		
		MimeMessage message = null;
		
		for (int i = 0; i < attachments.size(); i++) {
			File attachment = attachments.get(i);
			addedAttachments.add(attachment);
			messageSize += attachment.length();
			
			if (i == attachments.size() - 1 || messageSize + attachments.get(i + 1).length() > config.getSmtpMailGroupAttachmentsMaxSize() * 1024) {
				message = createMessage(session, addedAttachments, recipients);
				
				for (String recipient : recipients) {
					message.addRecipient(RecipientType.TO, new InternetAddress(recipient));
				}
				
				transport.sendMessage(message, message.getAllRecipients());
				
				LOGGER.info("Mail zostal wyslany prawidlowo.");
				
				if (archiveAttachments) {
					mediator.archive(addedAttachments);
				}
				
				messageSize = 0;
				
				addedAttachments.clear();
			}
		}
	}
	
	private Session getSession() {
		Properties properties = new Properties();
		
		properties.put(MAIL_SMTP_HOST_PROPERTY, config.getSmtpServerHost());
		properties.put(MAIL_SMTP_PORT_PROPERTY, config.getSmtpServerPort());
		properties.put(MAIL_SMTP_STARTTLS_ENABLE_PROPERTY, config.isSmtpServerRequiresSSL());
		
		return Session.getInstance(properties, null);
	}
}
