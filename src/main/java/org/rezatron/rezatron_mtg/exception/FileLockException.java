package org.rezatron.rezatron_mtg.exception;

public class FileLockException extends RuntimeException {

    public FileLockException(String message) {
        super(message);
    }

    public FileLockException(String message, Throwable cause) {
        super(message, cause);
    }
}