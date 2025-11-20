package com.mock.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class MockServiceConfig {
    private String id;
    private String serviceName;
    private int port;
    private String contextPath;
    private String description;
    private boolean running;

    private List<MockRule> rules = new ArrayList<>();
}
