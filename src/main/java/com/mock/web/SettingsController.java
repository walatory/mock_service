package com.mock.web;

import com.mock.model.AppSettings;
import com.mock.service.SettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    
    private final SettingsService settingsService;
    
    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
    
    @GetMapping
    public AppSettings getSettings() {
        return settingsService.getSettings();
    }
    
    @PutMapping
    public AppSettings updateSettings(@RequestBody AppSettings settings) {
        return settingsService.updateSettings(settings);
    }
}
