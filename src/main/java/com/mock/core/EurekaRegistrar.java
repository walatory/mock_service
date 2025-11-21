package com.mock.core;

import com.mock.model.MockServiceConfig;
import com.mock.service.SettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EurekaRegistrar {

    private final SettingsService settingsService;
    private final RestTemplate restTemplate = new RestTemplate();

    public EurekaRegistrar(SettingsService settingsService) {
        this.settingsService = settingsService;
    }



    public void register(MockServiceConfig config) {
        try {
            String eurekaUrl = settingsService.getSettings().getEurekaUrl();
            boolean preferIp = settingsService.getSettings().isPreferIpAddress();
            
            String appName = config.getServiceName().toUpperCase();
            String hostName = InetAddress.getLocalHost().getHostName();
            String ipAddr = InetAddress.getLocalHost().getHostAddress();
            
            // Use IP or hostname based on preference
            String addressToUse = preferIp ? ipAddr : hostName;
            String instanceId = addressToUse + ":" + appName + ":" + config.getPort();

            // Clean up eureka URL
            String url = eurekaUrl.endsWith("/") ? eurekaUrl : eurekaUrl + "/";
            url = url + "apps/" + appName;

            Map<String, Object> instance = new HashMap<>();
            instance.put("instanceId", instanceId);
            instance.put("hostName", preferIp ? ipAddr : hostName);
            instance.put("app", appName);
            instance.put("ipAddr", ipAddr);
            instance.put("status", "UP");
            instance.put("overriddenStatus", "UNKNOWN");
            instance.put("port", Map.of("$", config.getPort(), "@enabled", "true"));
            instance.put("securePort", Map.of("$", 443, "@enabled", "false"));
            instance.put("countryId", 1);
            instance.put("dataCenterInfo", Map.of(
                    "@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                    "name", "MyOwn"));
            instance.put("homePageUrl", "http://" + addressToUse + ":" + config.getPort() + "/");
            instance.put("statusPageUrl", "http://" + addressToUse + ":" + config.getPort() + "/info");
            instance.put("healthCheckUrl", "http://" + addressToUse + ":" + config.getPort() + "/health");
            instance.put("vipAddress", appName);
            instance.put("secureVipAddress", appName);

            Map<String, Object> body = new HashMap<>();
            body.put("instance", instance);

            try {
                restTemplate.postForObject(url, body, Void.class);
                log.info("Registered {} with Eureka at {} (using {})", appName, url, preferIp ? "IP" : "hostname");

                // Start heartbeat in a separate thread (simplified)
                startHeartbeat(appName, instanceId);
            } catch (Exception e) {
                log.warn("Failed to register {} with Eureka: {}", appName, e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error preparing Eureka registration", e);
        }
    }



    public void deregister(MockServiceConfig config) {
        try {
            String eurekaUrl = settingsService.getSettings().getEurekaUrl();
            boolean preferIp = settingsService.getSettings().isPreferIpAddress();
            
            String appName = config.getServiceName().toUpperCase();
            String hostName = InetAddress.getLocalHost().getHostName();
            String ipAddr = InetAddress.getLocalHost().getHostAddress();
            String addressToUse = preferIp ? ipAddr : hostName;
            String instanceId = addressToUse + ":" + appName + ":" + config.getPort();

            String url = eurekaUrl.endsWith("/") ? eurekaUrl : eurekaUrl + "/";
            url = url + "apps/" + appName + "/" + instanceId;

            restTemplate.delete(url);
            log.info("Deregistered {} from Eureka", appName);
        } catch (Exception e) {
            log.error("Failed to deregister from Eureka", e);
        }
    }



    private void startHeartbeat(String appName, String instanceId) {
        // In a real app, we'd manage this thread better
        new Thread(() -> {
            while (true) {
                try {
                    int interval = settingsService.getSettings().getHeartbeatIntervalSeconds();
                    Thread.sleep(interval * 1000L);
                    
                    String eurekaUrl = settingsService.getSettings().getEurekaUrl();
                    String url = eurekaUrl.endsWith("/") ? eurekaUrl : eurekaUrl + "/";
                    url = url + "apps/" + appName + "/" + instanceId;
                    restTemplate.put(url, null);
                    log.debug("Sent heartbeat for {}", instanceId);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    log.warn("Heartbeat failed for {}", instanceId);
                }
            }
        }).start();
    }
}

