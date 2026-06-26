package com.shopmanager.settings.service;

import com.shopmanager.settings.entity.ShopSettings;
import com.shopmanager.settings.repository.ShopSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsProvider {

    private final ShopSettingsRepository repository;

    public ShopSettings getSettings() {
        return repository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Shop settings not configured"));
    }

    public Double getGstPercentage() {
        return getSettings().getGstPercentage();
    }
}