package org.rezatron.rezatron_mtg.exception;

public class FileAlreadyUpToDateException extends RuntimeException {
    public FileAlreadyUpToDateException(String message) {
        super(message);
    }
}