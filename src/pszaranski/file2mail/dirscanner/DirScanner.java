package pszaranski.file2mail.dirscanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import pszaranski.file2mail.Mediator;
import pszaranski.file2mail.configuration.Configuration;
import pszaranski.file2mail.exception.DirScannerException;
import pszaranski.file2mail.exception.MailSenderException;
import pszaranski.file2mail.mailsender.Utils;

public class DirScanner implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(DirScanner.class.getName());
	
	private final Configuration config;
	private final Mediator mediator;
	private File workDir;
	private Thread dirScannerThread;
	private volatile boolean work;
	
	public DirScanner(Mediator mediator) {
		this.mediator = mediator;
		this.config = mediator.getConfiguration();
	}
	
	public void init() throws DirScannerException {
		LOGGER.log(Level.FINE, "Inicjalizacja modulu skanowania");
		
		this.workDir = new File(config.getWorkDirPath());
		
		if (!this.workDir.exists() && config.isWorkDirCreateOnStartup()) {
			if (!workDir.mkdir()) {
				throw new DirScannerException("Blad podczas tworzenia katalogu roboczego: " + config.getWorkDirPath());
			}
		} else if (!this.workDir.exists() && !this.config.isWorkDirCreateOnStartup()) {
			throw new DirScannerException("Brak katalogu roboczego: " + config.getWorkDirPath());
		}
		
		LOGGER.log(Level.INFO, "Inicjalizacja modulu skanowania zakonczona sukcesem");
	}
	
	public synchronized void start() {
		this.work = true;
		this.dirScannerThread = new Thread(this, "DirScannerThread");
		this.dirScannerThread.start();
	}
	
	public synchronized void stop() {
		LOGGER.log(Level.INFO, "Konczenie pracy skanera katalogu: " + workDir.getPath());
		this.work = false;
		
		try {
			this.dirScannerThread.join(config.getWorkDirScanInterval() * 5);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Przerwane oczekiwanie na wykonanie pracy skanera.", e);
		}
	}
	
	@Override
	public void run() {
		LOGGER.log(Level.INFO, "Skaner katalogu roboczego uruchomiony");
		while (work) {
			try {
				FileFilter fileFilter = new FileFilter() {
					public boolean accept(File pathname) {
						LOGGER.log(Level.FINE, "Prosba o akceptacje pliku: " + pathname.getName());
						boolean accept = false;
				
						if (pathname.isFile()) {
							RandomAccessFile raf = null;
							FileLock rafl = null;
							
							try {
								if (config.isWorkDirScanLockFiles()) {
									LOGGER.log(Level.FINE, "Otwieram plik: " + pathname.getName());
									raf = new RandomAccessFile(pathname, "rw");
									FileChannel rafc = raf.getChannel();
									
									LOGGER.log(Level.FINE, "Zakladam blokade: " + pathname.getName());
									rafl = rafc.lock();
									LOGGER.log(Level.FINE, "Zalozenie blokady na pliku powiodlo sie: " + pathname.getName());
								}
								
								if (rafl != null || !config.isWorkDirScanLockFiles()) {
									long fileSize1 = pathname.length();
									Thread.sleep(config.getWorkDirScanSingleFileInterval() * 1000);
									long fileSize2 = pathname.length();
									
									if (fileSize1 == fileSize2) {
										LOGGER.log(Level.FINE, "Rozmiar pliku nie zmienil sie i wynosi: " + Utils.formatSize(fileSize2));
										
										if (pathname.length() < config.getSmtpMailGroupAttachmentsMaxSize() * 1024) {
											LOGGER.log(Level.FINE, "Rozmiar pliku : " + pathname.getName() + " jest prawidlowy. Plik zaakceptowany.");
											accept = true;
										} else {
											LOGGER.log(Level.WARNING, "Rozmiar pliku: " + pathname.getName() + " przekracza skonfigurowany maksymalny rozmiar pliku: " + Utils.formatSize(config.getSmtpMailGroupAttachmentsMaxSize() * 1024));
										}
									} else {
										LOGGER.log(Level.FINE, "Rozmiar pliku " + pathname.getName() + " sie zmienia: " + Utils.formatSize(fileSize1) + " i: " + Utils.formatSize(fileSize2));
									}
									
								} else if (rafl == null) {
									LOGGER.log(Level.FINE, "Zalozenie blokady na pliku nie powiodlo sie: " + pathname.getName());
								}
							} catch (Exception e) {
								LOGGER.log(Level.FINE, "Zalozenie blokady na pliku nie powiodlo sie: " + pathname.getName(), e);
							} finally {
								if (rafl != null) {
									try {
										rafl.release();
									} catch (IOException e) {
										LOGGER.log(Level.WARNING, "Blad wypuszczenia blokady na pliku: " + pathname.getPath(), e);
									}
								}
								
								if (raf != null) {
									try {
										raf.close();
									} catch (IOException e) {
										LOGGER.log(Level.WARNING, "Blad zamkniecia kanalu do pliku: " + pathname.getPath(), e);
									}
								}
							}
						}
						
						LOGGER.log(Level.FINE, "Prosba o akceptacje dla pliku: " + pathname.getName() + " zwrocilo: " + accept);
						return accept;
					}
				};
		
				LOGGER.log(Level.FINEST, "Rozpoczynam skanowanie katalogu roboczego");
				
				File[] attachments = workDir.listFiles(fileFilter);
				
				LOGGER.log(Level.FINEST, "Zakonczono skanowanie katalogu roboczego");
				
				if (attachments.length > 0) {
					StringBuilder sb = new StringBuilder();
					for (File attachment: attachments) {
						if (!sb.toString().isEmpty()) {
							sb.append(", ");
						}
						sb.append(attachment.getName());
					}
				
					LOGGER.log(Level.FINE, "Przefiltrowane pliki: " + sb.toString());
					
					try {
						mediator.sendMail(Arrays.asList(attachments));
					} catch (MailSenderException e) {
						LOGGER.log(Level.SEVERE, "Blad wyslania wiadomosci e-mail", e);
					}
				} else {
					LOGGER.log(Level.FINEST, "Brak plikow po filtracji");
				}
				
				try {
					LOGGER.log(Level.FINEST, "Zasypiam watek na: " + config.getWorkDirScanInterval() * 1000 + "ms");
					
					Thread.sleep(config.getWorkDirScanInterval() * 1000);
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, "Watek skanujacy katalog zostal nagle zatrzymany", e);
					work = false;
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Nieznany blad podczas dzialania aplikacji", e);
			}
		}
	}
}
