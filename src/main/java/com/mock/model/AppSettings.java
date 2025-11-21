package com.mock.model;

import lombok.Data;

@Data
public class AppSettings {
    private String eurekaUrl = "http://localhost:8761/eureka/";
    private boolean preferIpAddress = true;
    private int heartbeatIntervalSeconds = 30;
}
