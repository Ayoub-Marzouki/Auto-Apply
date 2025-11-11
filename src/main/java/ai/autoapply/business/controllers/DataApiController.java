package ai.autoapply.business.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.autoapply.database.domain.ParsedCV;
import ai.autoapply.database.service.ParsedCVService;

import java.util.List;

/**
 * This is the API for our Local Agent.
 */
@RestController
@RequestMapping("/api/v1")
public class DataApiController {

    private final ParsedCVService parsedCVService;

    public DataApiController(ParsedCVService parsedCVService) {
        this.parsedCVService = parsedCVService;
    }

    @GetMapping("/cv-data")
    public ResponseEntity<List<ParsedCV>> getAllCvData() {
        List<ParsedCV> allCVs = parsedCVService.getAllParsedCVs();
        
        return ResponseEntity.ok(allCVs);
    }
}