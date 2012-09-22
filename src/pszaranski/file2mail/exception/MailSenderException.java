package pszaranski.file2mail.exception;

public class MailSenderException extends Exception {
	private static final long serialVersionUID = 1L;

	public MailSenderException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailSenderException(String message) {
		super(message);
	}
}
