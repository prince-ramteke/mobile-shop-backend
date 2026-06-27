package com.shopmanager.settings.controller;

import com.shopmanager.settings.dto.ShopSettingsRequest;
import com.shopmanager.settings.dto.ShopSettingsResponse;
import com.shopmanager.settings.service.ShopSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ShopSettingsService shopSettingsService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<ShopSettingsResponse> getSettings() {
        return ResponseEntity.ok(shopSettingsService.getSettings());
    }

    @PutMapping
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ShopSettingsResponse> updateSettings(
            @Valid @RequestBody ShopSettingsRequest request
    ) {
        return ResponseEntity.ok(shopSettingsService.updateSettings(request));
    }
}