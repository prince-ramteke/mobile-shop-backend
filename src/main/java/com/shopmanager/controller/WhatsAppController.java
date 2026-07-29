package com.shopmanager.controller;

import com.shopmanager.dto.whatsapp.WhatsAppMessageResponse;
import com.shopmanager.dto.whatsapp.WhatsAppTemplateResponse;
import com.shopmanager.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WhatsApp Controller - Handles WhatsApp message operations
 * including invoice sharing, repair updates, and message previews.
 *
 * NOTE: authorization intentionally left open to stay consistent with the rest
 * of the API (all /api/** is permitAll in SecurityConfig). A lone @PreAuthorize
 * here would make these the only endpoints returning 403 on a missing/expired
 * JWT — the same trap that broke the Settings module.
 */
@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/invoice/{saleId}")
    public ResponseEntity<WhatsAppMessageResponse> sendInvoiceWhatsApp(
            @PathVariable Long saleId) {
        return ResponseEntity.ok(whatsAppService.sendInvoice(saleId));
    }

    @PostMapping("/repair/{repairId}")
    public ResponseEntity<WhatsAppMessageResponse> sendRepairStatusWhatsApp(
            @PathVariable Long repairId) {
        return ResponseEntity.ok(whatsAppService.sendRepairUpdate(repairId));
    }

    @GetMapping("/preview/{type}/{id}")
    public ResponseEntity<WhatsAppMessageResponse> previewMessage(
            @PathVariable String type,
            @PathVariable Long id) {
        return ResponseEntity.ok(whatsAppService.previewMessage(type, id));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<WhatsAppTemplateResponse>> getTemplates() {
        return ResponseEntity.ok(whatsAppService.getTemplates());
    }

    @PutMapping("/templates/{templateId}")
    public ResponseEntity<WhatsAppTemplateResponse> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody WhatsAppTemplateResponse templateData) {
        return ResponseEntity.ok(whatsAppService.updateTemplate(templateId, templateData));
    }
}