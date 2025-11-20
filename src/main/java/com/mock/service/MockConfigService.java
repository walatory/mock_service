package com.mock.service;

import com.mock.core.DynamicServerManager;
import com.mock.model.MockServiceConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockConfigService {

    private final Map<String, MockServiceConfig> configs = new ConcurrentHashMap<>();
    private final DynamicServerManager serverManager;

    public MockConfigService(DynamicServerManager serverManager) {
        this.serverManager = serverManager;
    }

    public List<MockServiceConfig> getAllServices() {
        return new ArrayList<>(configs.values());
    }

    public MockServiceConfig getService(String id) {
        return configs.get(id);
    }

    public MockServiceConfig createService(MockServiceConfig config) {
        if (config.getId() == null) {
            config.setId(UUID.randomUUID().toString());
        }
        configs.put(config.getId(), config);
        return config;
    }

    public MockServiceConfig updateService(MockServiceConfig config) {
        configs.put(config.getId(), config);
        // If running, restart to apply changes (simplified)
        if (serverManager.isRunning(config.getId())) {
            serverManager.stopService(config);
            serverManager.startService(config);
        }
        return config;
    }

    public void deleteService(String id) {
        MockServiceConfig config = configs.remove(id);
        if (config != null && serverManager.isRunning(id)) {
            serverManager.stopService(config);
        }
    }

    public void startService(String id) {
        MockServiceConfig config = configs.get(id);
        if (config != null) {
            serverManager.startService(config);
        }
    }

    public void stopService(String id) {
        MockServiceConfig config = configs.get(id);
        if (config != null) {
            serverManager.stopService(config);
        }
    }
}
