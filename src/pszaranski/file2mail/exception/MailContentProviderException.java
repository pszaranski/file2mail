package pszaranski.file2mail.exception;

public class MailContentProviderException extends Exception {
	private static final long serialVersionUID = 1L;

	public MailContentProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailContentProviderException(String message) {
		super(message);
	}
}
