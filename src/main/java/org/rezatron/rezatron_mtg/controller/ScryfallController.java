package org.rezatron.rezatron_mtg.controller;



import lombok.extern.slf4j.Slf4j;

import org.rezatron.rezatron_mtg.dto.ApiResponse;
import org.rezatron.rezatron_mtg.dto.ScryfallResponse;
import org.rezatron.rezatron_mtg.service.ScryfallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ScryfallController {

    @Autowired
    private ScryfallService scryfallService;

    @GetMapping("/check-bulk-data")
    public ResponseEntity<ApiResponse<ScryfallResponse>> checkBulkData() {
        log.info("Received request to check bulk data.");
        try {
            ScryfallResponse scryfallResponse = scryfallService.checkAndDownloadBulkData();
            ApiResponse<ScryfallResponse> response = new ApiResponse<>(true, "Operation successful", scryfallResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error occurred while checking bulk data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}