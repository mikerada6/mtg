package org.rezatron.rezatron_mtg.exception;



public class FileDownloadFailedException extends RuntimeException {
    public FileDownloadFailedException(String message) {
        super(message);
    }
}