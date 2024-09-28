package org.rezatron.rezatron_mtg.controller;



import lombok.extern.slf4j.Slf4j;

import org.rezatron.rezatron_mtg.dto.ApiResponse;
import org.rezatron.rezatron_mtg.dto.ScryfallResponse;
import org.rezatron.rezatron_mtg.service.ScryfallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/scryfall")
@Tag(name = "Scryfall API", description = "Operations related to Scryfall data fetching")

public class ScryfallController {

    @Autowired
    private ScryfallService scryfallService;
    
    @Operation(summary = "Check and download bulk data", description = "Fetches and checks bulk data from Scryfall, and downloads it if necessary.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operation successful", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScryfallResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
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