package com.mock.controller;

import com.mock.dto.SwaggerEndpoint;
import com.mock.dto.SwaggerImportRequest;
import com.mock.service.SwaggerParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/swagger")
@RequiredArgsConstructor
public class SwaggerImportController {

    private final SwaggerParser swaggerParser;

    /**
     * Parse Swagger document and return available endpoints
     */
    @PostMapping("/parse")
    public List<SwaggerEndpoint> parseSwagger(@RequestBody SwaggerImportRequest request) {
        log.info("Received Swagger import request for URL: {}", request.getSwaggerUrl());
        return swaggerParser.parseSwaggerUrl(request.getSwaggerUrl());
    }
}
