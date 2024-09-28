package org.rezatron.rezatron_mtg.exception;

import org.rezatron.rezatron_mtg.dto.ApiResponse;
import org.rezatron.rezatron_mtg.dto.ScryfallResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileAlreadyUpToDateException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileAlreadyUpToDateException(FileAlreadyUpToDateException ex) {
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK, because nothing went wrong
    }

    @ExceptionHandler(FileDownloadFailedException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileDownloadFailedException(FileDownloadFailedException ex) {
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500, download failed
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>(false, "An unexpected error occurred: " + ex.getMessage(),
                null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500, for general errors
    }

    @ExceptionHandler(FileLockException.class)
    public ResponseEntity<ScryfallResponse> handleFileLockException(FileLockException ex) {
        ScryfallResponse response = new ScryfallResponse(
                false,
                "Lock acquisition failed: " + ex.getMessage(),
                false,
                0, // no file size available
                null, // no download URL
                false // no re-download
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 Conflict
    }
}