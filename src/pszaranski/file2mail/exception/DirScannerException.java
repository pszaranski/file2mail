package pszaranski.file2mail.exception;

public class DirScannerException extends Exception {
	private static final long serialVersionUID = 1L;

	public DirScannerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DirScannerException(String message) {
		super(message);
	}
}
