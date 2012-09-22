package pszaranski.file2mail.exception;

public class ArchiverException extends Exception {
	private static final long serialVersionUID = 1L;

	public ArchiverException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiverException(String message) {
		super(message);
	}
}
