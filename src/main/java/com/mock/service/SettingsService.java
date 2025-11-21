package com.mock.service;

import com.mock.model.AppSettings;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    
    private AppSettings settings = new AppSettings();
    
    public AppSettings getSettings() {
        return settings;
    }
    
    public AppSettings updateSettings(AppSettings newSettings) {
        this.settings = newSettings;
        return settings;
    }
}
