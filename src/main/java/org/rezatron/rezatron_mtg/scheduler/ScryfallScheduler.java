package org.rezatron.rezatron_mtg.scheduler;

import lombok.extern.slf4j.Slf4j;

import org.rezatron.rezatron_mtg.service.ScryfallService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScryfallScheduler {

    private final ScryfallService scryfallService;

    public ScryfallScheduler(ScryfallService scryfallService) {
        this.scryfallService = scryfallService;
    }

    // Scheduled to run every hour on the hour
    @Scheduled(cron = "0 0 * * * ?")  // Cron expression for every hour on the hour
    public void runScheduledDownload() {
        try {
            log.info("Starting scheduled download task");
            scryfallService.checkAndDownloadBulkData();
            log.info("Scheduled download task completed");
        } catch (Exception e) {
            log.error("Error occurred during the scheduled download task", e);
        }
    }
}