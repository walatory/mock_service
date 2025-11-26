package com.mock.model;

import lombok.Data;
import java.util.Map;

@Data
public class MockRule {
    private String id;
    private String method; // GET, POST, etc.
    private String urlPattern; // Ant path pattern e.g. /api/users/**
    private String bodyMatch; // Regex or simple match

    private int responseStatus = 200;
    private String responseBody; // Template
    private Map<String, String> responseHeaders;

    private long delayMs; // Simulate latency

    private String mode = "mock"; // "mock" or "forward"
    private String forwardUrl; // Target URL for forwarding
}
