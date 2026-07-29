package com.shopmanager.settings.controller;

import com.shopmanager.settings.dto.ShopSettingsRequest;
import com.shopmanager.settings.dto.ShopSettingsResponse;
import com.shopmanager.settings.service.ShopSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ShopSettingsService shopSettingsService;

    // NOTE: Access to Settings is restricted to admins on the frontend
    // (ProtectedRoute adminOnly + isAdmin gate). Authorization is intentionally
    // left open here to stay consistent with the rest of the API, which is
    // currently permitAll (see SecurityConfig). Previously this was the ONLY
    // endpoint with an active @PreAuthorize, which caused the whole Settings
    // module to return 403 whenever the JWT was missing/expired while every
    // other page kept working.
    @GetMapping
    public ResponseEntity<ShopSettingsResponse> getSettings() {
        return ResponseEntity.ok(shopSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<ShopSettingsResponse> updateSettings(
            @Valid @RequestBody ShopSettingsRequest request
    ) {
        return ResponseEntity.ok(shopSettingsService.updateSettings(request));
    }
}