package com.mock.controller;

import com.mock.service.MockDataGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mock")
@RequiredArgsConstructor
public class MockDataController {

    private final MockDataGenerator mockDataGenerator;

    /**
     * Generate mock data from JSON schema
     */
    @PostMapping("/generate")
    public Object generateMockData(@RequestBody Map<String, Object> schema) {
        log.info("Generating mock data from schema");
        return mockDataGenerator.generateFromSchema(schema);
    }
}
