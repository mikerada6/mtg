package org.rezatron.rezatron_mtg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScryfallResponse {

    private boolean success;
    private String message;
    private boolean fileDownloaded;
    private long fileSize;
    private String downloadUrl;     // Include the URL used for download
    private boolean isRedownload;   // Mark whether it was a fresh download or a re-download
}