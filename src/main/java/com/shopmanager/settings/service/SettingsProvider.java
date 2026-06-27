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
                .orElseGet(() -> repository.save(
                        ShopSettings.builder()
                                .shopName("Saurabh Mobile Shop")
                                .shopPhone("")
                                .shopAddress("")
                                .gstNumber("")
                                .invoiceFooter("Thank you for your business!")
                                .whatsappEnabled(true)
                                .reminderGapDays(3)
                                .gstPercentage(18.0)
                                .build()
                ));
    }

    public Double getGstPercentage() {
        return getSettings().getGstPercentage();
    }
}