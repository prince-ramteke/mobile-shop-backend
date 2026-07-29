package com.shopmanager.settings.service.impl;

import com.shopmanager.settings.dto.ShopSettingsRequest;
import com.shopmanager.settings.dto.ShopSettingsResponse;
import com.shopmanager.settings.entity.ShopSettings;
import com.shopmanager.settings.repository.ShopSettingsRepository;
import com.shopmanager.settings.service.SettingsProvider;
import com.shopmanager.settings.service.ShopSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopSettingsServiceImpl implements ShopSettingsService {

    private final SettingsProvider settingsProvider;
    private final ShopSettingsRepository repository;

    @Override
    public ShopSettingsResponse getSettings() {

        ShopSettings settings = repository.findAll()
                .stream()
                .findFirst()
                .orElseGet(this::createDefault);

        return toResponse(settings);
    }

    @Override
    public ShopSettingsResponse updateSettings(ShopSettingsRequest req) {

        ShopSettings settings = repository.findAll()
                .stream()
                .findFirst()
                .orElseGet(this::createDefault);

        settings.setShopName(req.getShopName());
        settings.setShopPhone(req.getShopPhone());
        settings.setShopAddress(req.getShopAddress());
        settings.setGstNumber(req.getGstNumber());
        settings.setEmail(req.getEmail());
        settings.setInvoiceFooter(req.getInvoiceFooter());

        if (req.getGstPercentage() != null)
            settings.setGstPercentage(req.getGstPercentage());

        settings.setWhatsappEnabled(
                req.getWhatsappEnabled() != null ? req.getWhatsappEnabled() : false);

        settings.setReminderGapDays(
                req.getReminderGapDays() != null ? req.getReminderGapDays() : 3);

        ShopSettings saved = repository.save(settings);

        return toResponse(saved);
    }

    // ---------------- PRIVATE ----------------

    private ShopSettings createDefault() {
        return repository.save(
                ShopSettings.builder()
                        .shopName("Saurabh Mobile Shop")
                        .gstPercentage(18.0)
                        .whatsappEnabled(true)
                        .reminderGapDays(3)
                        .build()
        );
    }

    private ShopSettingsResponse toResponse(ShopSettings s) {
        return ShopSettingsResponse.builder()
                .id(s.getId())
                .gstPercentage(s.getGstPercentage())   // ADD THIS
                .shopName(s.getShopName())
                .shopPhone(s.getShopPhone())
                .shopAddress(s.getShopAddress())
                .gstNumber(s.getGstNumber())
                .email(s.getEmail())
                .invoiceFooter(s.getInvoiceFooter())
                .whatsappEnabled(s.getWhatsappEnabled())
                .reminderGapDays(s.getReminderGapDays())
                .build();
    }
}