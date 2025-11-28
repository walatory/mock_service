package com.mock.web;

import com.mock.model.MockServiceConfig;
import com.mock.service.MockConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ConfigController {

    private final MockConfigService configService;

    public ConfigController(MockConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public List<MockServiceConfig> list() {
        return configService.getAllServices();
    }

    @PostMapping
    public MockServiceConfig create(@RequestBody MockServiceConfig config) {
        return configService.createService(config);
    }

    @PutMapping("/{id}")
    public MockServiceConfig update(@PathVariable String id, @RequestBody MockServiceConfig config) {
        config.setId(id);
        return configService.updateService(config);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        configService.deleteService(id);
    }

    @PostMapping("/{id}/start")
    public void start(@PathVariable String id) {
        configService.startService(id);
    }

    @PostMapping("/{id}/stop")
    public void stop(@PathVariable String id) {
        configService.stopService(id);
    }

    @GetMapping("/export")
    public List<MockServiceConfig> export() {
        return configService.getAllServices();
    }

    @PostMapping("/import")
    public void importConfigs(@RequestBody List<MockServiceConfig> configs) {
        for (MockServiceConfig config : configs) {
            if (config.getId() != null) {
                configService.updateService(config);
            } else {
                configService.createService(config);
            }
        }
    }
}
