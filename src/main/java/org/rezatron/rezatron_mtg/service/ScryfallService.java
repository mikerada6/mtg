package org.rezatron.rezatron_mtg.service;

import lombok.extern.slf4j.Slf4j;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.rezatron.rezatron_mtg.dto.ScryfallResponse;
import org.rezatron.rezatron_mtg.entity.DownloadLog;
import org.rezatron.rezatron_mtg.entity.FileLock;
import org.rezatron.rezatron_mtg.exception.FileLockException;
import org.rezatron.rezatron_mtg.repository.DownloadLogRepository;
import org.rezatron.rezatron_mtg.repository.FileLockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class ScryfallService {

    private static final String BULK_DATA_URL = "https://api.scryfall.com/bulk-data";
    private static final String DEFAULT_CARDS_ID = "default_cards";

    @Value("${spring.application.name}")
    private String instanceId; // Unique identifier for each instance of the app

    @Value("${scryfall.local-file-path}")
    private String localDirectoryPath;

    private final DownloadLogRepository downloadLogRepository;
    private final FileLockRepository fileLockRepository;

    public ScryfallService(DownloadLogRepository downloadLogRepository, FileLockRepository fileLockRepository) {
        this.downloadLogRepository = downloadLogRepository;
        this.fileLockRepository = fileLockRepository;
    }

    @Transactional
    public ScryfallResponse checkAndDownloadBulkData() throws Exception {
        String lockId = DEFAULT_CARDS_ID; // Consistent lock identifier
        String fileName = null;

        // Step 1: Attempt to acquire lock using consistent identifier
        log.info("Attempting to acquire lock for file: {}", lockId);
        if (!acquireLock(lockId)) {
            log.warn("Lock acquisition failed. Another instance is already processing the file: {}", lockId);
            throw new FileLockException("Another instance is already processing the file: " + lockId);
        }
        log.info("Lock successfully acquired for file: {}", lockId);

        log.info("Starting process to check and download bulk data from Scryfall API.");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(BULK_DATA_URL);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            log.info("Successfully fetched bulk data from Scryfall API.");

            // Step 2: Parse the response
            String result = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode bulkData = mapper.readTree(result);

            // Step 3: Extract the correct node (default_cards)
            JsonNode defaultCardsNode = findDefaultCardsNode(bulkData);
            if (defaultCardsNode == null) {
                log.error("Default Cards node not found in API response.");
                throw new Exception("Default Cards node not found in API response.");
            }

            // Step 4: Extract download URL and file size from the API response
            String downloadUrl = defaultCardsNode.get("download_uri").asText();
            long apiFileSize = defaultCardsNode.get("size").asLong();
            log.info("Download URL: {}, Expected File Size: {}", downloadUrl, apiFileSize);

            // Step 5: Extract file name from URL
            fileName = extractFileNameFromUrl(downloadUrl);
            log.info("Extracted file name from URL: {}", fileName);

            // Step 6: Check if file already exists and compare sizes
            Path localFilePath = Paths.get(localDirectoryPath, fileName);
            boolean isRedownload = false;

            if (Files.exists(localFilePath)) {
                long localFileSize = Files.size(localFilePath);
                if (localFileSize == apiFileSize) {
                    log.info("File is already up-to-date: {}. No need to download.", fileName);
                    releaseLock(lockId);
                    return new ScryfallResponse(true, "File is already up-to-date.", false, localFileSize, downloadUrl,
                            isRedownload);
                } else {
                    log.warn("File size mismatch detected. Re-downloading file: {}", fileName);
                    isRedownload = true;
                }
            } else {
                log.info("File does not exist. Proceeding with download: {}", fileName);
            }

            // Step 7: Download the file
            downloadFile(downloadUrl, localFilePath, apiFileSize);

            // Step 8: Log the file download as fresh or re-download
            logDownload(fileName, isRedownload, downloadUrl);
            log.info("File {} successfully downloaded and logged.", fileName);

            // Step 9: Release the lock after completing the download
            releaseLock(lockId);
            log.info("Lock released for file: {}", lockId);

            return new ScryfallResponse(true, "File downloaded successfully.", true, apiFileSize, downloadUrl,
                    isRedownload);
        } catch (Exception e) {
            if (lockId != null) {
                log.error("An error occurred while processing file: {}. Releasing lock.", lockId, e);
                releaseLock(lockId); // Ensure lock is released in case of an exception
            }
            throw e;
        }
    }

    private boolean acquireLock(String lockId) {
        Optional<FileLock> existingLock = fileLockRepository.findById(lockId);
        if (existingLock.isPresent() && existingLock.get().isLocked()) {
            log.warn("Lock is already held by another instance for file: {}", lockId);
            return false; // Another instance holds the lock
        }

        // Acquire the lock
        FileLock lock = new FileLock(lockId, instanceId, LocalDateTime.now(), true);
        fileLockRepository.save(lock);
        log.info("Lock acquired for file: {}", lockId);
        return true;
    }

    private void releaseLock(String lockId) {
        Optional<FileLock> lock = fileLockRepository.findById(lockId);
        lock.ifPresent(fileLock -> {
            fileLock.setLocked(false);
            fileLockRepository.save(fileLock);
            log.info("Lock successfully released for file: {}", lockId);
        });
    }

    private String extractFileNameFromUrl(String downloadUrl) throws Exception {
        URL url = new URL(downloadUrl);
        return Paths.get(url.getPath()).getFileName().toString();
    }

    private JsonNode findDefaultCardsNode(JsonNode bulkData) {
        for (JsonNode node : bulkData.get("data")) {
            if (DEFAULT_CARDS_ID.equals(node.get("type").asText())) {
                return node;
            }
        }
        return null;
    }

    private void downloadFile(String downloadUrl, Path localFilePath, long expectedSize) throws Exception {
        log.info("Starting download from URL: {}", downloadUrl);
        try (InputStream in = new URL(downloadUrl).openStream();
                FileOutputStream out = new FileOutputStream(localFilePath.toFile())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            log.info("Download completed. Total bytes downloaded: {}. Expected size: {}", totalBytesRead, expectedSize);
            if (totalBytesRead != expectedSize) {
                log.error("File size mismatch after download. Expected: {}, Got: {}", expectedSize, totalBytesRead);
                throw new Exception("Downloaded file size does not match the expected size.");
            }
        }
    }

    private void logDownload(String fileName, boolean isRedownload, String downloadUrl) {
        DownloadLog downloadLog = new DownloadLog();
        downloadLog.setFileName(fileName);
        downloadLog.setDownloadedAt(LocalDateTime.now());
        downloadLog.setRedownload(isRedownload); // Mark as fresh download or re-download
        downloadLog.setDownloadUrl(downloadUrl); // Log the URL used for download
        downloadLogRepository.save(downloadLog);

        if (isRedownload) {
            log.info("Re-download logged for file: {}", fileName);
        } else {
            log.info("Fresh download logged for file: {}", fileName);
        }
    }
}