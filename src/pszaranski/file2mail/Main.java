package pszaranski.file2mail;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import pszaranski.file2mail.exception.ArchiverException;
import pszaranski.file2mail.exception.ConfigurationException;
import pszaranski.file2mail.exception.DirScannerException;
import pszaranski.file2mail.exception.MailContentProviderException;
import pszaranski.file2mail.exception.MailSenderException;


public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
//		try {
//			FileHandler fileHandler = new FileHandler(args[0], 10000, 100);
//			fileHandler.setEncoding("UTF-8");
//			fileHandler.setLevel(Level.ALL);
//			fileHandler.setFormatter(new SimpleFormatter());
//			
//			Logger.getLogger("").addHandler(fileHandler);
//		} catch (Exception e) {
//			System.out.println("Blad konfiguracji loggera aplikacji. Aplikacja zostanie zamknieta.");
//			e.printStackTrace();
//			System.exit(-1);
//		}
		
		LOGGER.log(Level.INFO, "### file2mail v1.0 # Piotr Szaranski (szaranski.piotr@gmail.com) ###");

		
		final Mediator mediator = new Mediator(args[0]);
		
		try {
			mediator.init();
		} catch (ConfigurationException e) {
			LOGGER.log(Level.SEVERE, "Blad podczas inicjalizacji modulu konfiguracji.", e);
		} catch (DirScannerException e) {
			LOGGER.log(Level.SEVERE, "Blad podczas inicjalizacji modulu skanowania katalogu.", e);
		} catch (MailSenderException e) {
			LOGGER.log(Level.SEVERE, "Blad podczas inicjalizacji modulu wysylania maili.", e);
		} catch (ArchiverException e) {
			LOGGER.log(Level.SEVERE, "Blad podczas inicjalizacji modulu archiwizatora.", e);
		} catch (MailContentProviderException e) {
			LOGGER.log(Level.SEVERE, "Blad podczas inicjalizacji modulu dostawcy tresci maili.", e);
		}
		
		if (!mediator.isInit()) {
			System.out.println("Nastapil powazny blad podczas inicjalizowania aplikacji. Aplikacja zostanie zamknieta.");
			System.exit(-1);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				LOGGER.log(Level.INFO, "Zazadano zamkniecia aplikacji.");
				mediator.stopApp();
			}
		}, "file2mail shutdown hook"));
		
		mediator.startApp();
	}
}
