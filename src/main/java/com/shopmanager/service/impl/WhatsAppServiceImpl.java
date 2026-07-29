package com.shopmanager.service.impl;

import com.shopmanager.dto.whatsapp.WhatsAppMessageResponse;
import com.shopmanager.dto.whatsapp.WhatsAppTemplateResponse;
import com.shopmanager.entity.Customer;
import com.shopmanager.entity.RepairJob;
import com.shopmanager.entity.Sale;
import com.shopmanager.entity.TemplateType;
import com.shopmanager.entity.WhatsAppHistory;
import com.shopmanager.exception.ResourceNotFoundException;
import com.shopmanager.message.sender.whatsapp.WhatsAppApiClient;
import com.shopmanager.message.sender.whatsapp.WhatsAppProperties;
import com.shopmanager.repository.CustomerRepository;
import com.shopmanager.repository.RepairJobRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.repository.WhatsAppHistoryRepository;
import com.shopmanager.repository.WhatsAppTemplateRepository;
import com.shopmanager.service.WhatsAppService;
import com.shopmanager.settings.service.SettingsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final WhatsAppTemplateRepository templateRepository;
    private final WhatsAppHistoryRepository historyRepository;
    private final WhatsAppApiClient apiClient;
    private final WhatsAppProperties properties;
    private final SaleRepository saleRepository;
    private final RepairJobRepository repairJobRepository;
    private final CustomerRepository customerRepository;
    private final SettingsProvider settingsProvider;

    @Value("${whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    private boolean isConfigured() {
        return whatsappEnabled
                && notBlank(properties.getToken())
                && notBlank(properties.getPhoneNumberId());
    }

    // ---------------- PUBLIC API ----------------

    @Override
    @Transactional
    public WhatsAppMessageResponse sendInvoice(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + saleId));
        Customer customer = sale.getCustomer();
        String message = renderInvoice(sale, customer);
        return dispatch(phoneOf(customer), TemplateType.INVOICE, message);
    }

    @Override
    @Transactional
    public WhatsAppMessageResponse sendRepairUpdate(Long repairId) {
        RepairJob job = repairJobRepository.findByIdWithCustomer(repairId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair job not found: " + repairId));
        String message = renderRepair(job, job.getCustomer());
        return dispatch(phoneOf(job.getCustomer()), TemplateType.REPAIR_UPDATE, message);
    }

    @Override
    @Transactional
    public WhatsAppMessageResponse sendDueReminder(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        String message = renderDue(customer);
        return dispatch(phoneOf(customer), TemplateType.DUE_REMINDER, message);
    }

    @Override
    @Transactional(readOnly = true)
    public WhatsAppMessageResponse previewMessage(String type, Long id) {
        String message;
        switch (type == null ? "" : type.toLowerCase()) {
            case "invoice" -> {
                Sale sale = saleRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));
                message = renderInvoice(sale, sale.getCustomer());
            }
            case "repair" -> {
                RepairJob job = repairJobRepository.findByIdWithCustomer(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Repair job not found: " + id));
                message = renderRepair(job, job.getCustomer());
            }
            case "due" -> {
                Customer c = customerRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
                message = renderDue(c);
            }
            default -> message = "Unknown preview type: " + type;
        }
        return WhatsAppMessageResponse.builder()
                .success(true)
                .message("Preview generated")
                .preview(message)
                .status("PREVIEW")
                .build();
    }

    @Override
    public List<WhatsAppTemplateResponse> getTemplates() {
        return Collections.emptyList();
    }

    @Override
    public WhatsAppTemplateResponse updateTemplate(Long templateId, WhatsAppTemplateResponse templateData) {
        return templateData;
    }

    @Override
    public List<?> getReminderHistory(Long customerId) {
        return Collections.emptyList();
    }

    // ---------------- CORE DISPATCH ----------------

    private WhatsAppMessageResponse dispatch(String phone, TemplateType type, String message) {
        boolean sent = false;
        String status;
        String note;

        if (!isConfigured()) {
            status = "MOCK";
            note = "WhatsApp is disabled/unconfigured — message logged only, not sent.";
        } else if (!notBlank(phone)) {
            status = "NO_PHONE";
            note = "Customer has no phone/WhatsApp number on file — not sent.";
        } else {
            try {
                String providerId = apiClient.sendText(normalizePhone(phone), message);
                sent = true;
                status = "SENT";
                note = "WhatsApp message sent (id: " + providerId + ").";
            } catch (Exception e) {
                status = "FAILED";
                note = "WhatsApp send failed: " + e.getMessage();
                log.error("WhatsApp send failed for {}", phone, e);
            }
        }

        historyRepository.save(WhatsAppHistory.builder()
                .phone(phone)
                .type(type)
                .message(message)
                .success(sent)
                .sentAt(LocalDateTime.now())
                .build());

        // A logged mock is not an error from the UI's perspective; a configured
        // send that failed is.
        boolean uiSuccess = sent || "MOCK".equals(status) || "NO_PHONE".equals(status);

        return WhatsAppMessageResponse.builder()
                .success(uiSuccess)
                .message(note)
                .preview(message)
                .status(status)
                .build();
    }

    // ---------------- MESSAGE RENDERING ----------------

    private String renderInvoice(Sale sale, Customer customer) {
        String template = templateContent(TemplateType.INVOICE,
                "Dear {customer}, thank you for your purchase at {shop}. "
                        + "Invoice: {invoice}, Total: ₹{total}, Pending: ₹{pending}.");
        return template
                .replace("{customer}", nameOf(customer))
                .replace("{shop}", shopName())
                .replace("{invoice}", nz(sale.getInvoiceNumber()))
                .replace("{saleId}", String.valueOf(sale.getId()))
                .replace("{total}", money(sale.getGrandTotal()))
                .replace("{pending}", money(sale.getPendingAmount()));
    }

    private String renderRepair(RepairJob job, Customer customer) {
        String template = templateContent(TemplateType.REPAIR_UPDATE,
                "Dear {customer}, your repair {job} ({device}) status is now {status}. "
                        + "Pending: ₹{pending}. - {shop}");
        String device = (nz(job.getDeviceBrand()) + " " + nz(job.getDeviceModel())).trim();
        return template
                .replace("{customer}", nameOf(customer))
                .replace("{shop}", shopName())
                .replace("{job}", nz(job.getJobNumber()))
                .replace("{device}", device)
                .replace("{status}", job.getStatus() != null ? job.getStatus().name() : "")
                .replace("{pending}", money(job.getPendingAmount()));
    }

    private String renderDue(Customer customer) {
        String template = templateContent(TemplateType.DUE_REMINDER,
                "Dear {customer}, this is a friendly reminder about your pending due at {shop}. "
                        + "Please clear it at your convenience.");
        return template
                .replace("{customer}", nameOf(customer))
                .replace("{shop}", shopName());
    }

    private String templateContent(TemplateType type, String fallback) {
        return templateRepository.findByTypeAndEnabledTrue(type)
                .map(t -> t.getContent())
                .filter(this::notBlank)
                .orElse(fallback);
    }

    // ---------------- HELPERS ----------------

    private String shopName() {
        try {
            return nz(settingsProvider.getSettings().getShopName());
        } catch (Exception e) {
            return "our shop";
        }
    }

    private String phoneOf(Customer c) {
        return c != null ? c.getEffectiveWhatsappNumber() : null;
    }

    private String nameOf(Customer c) {
        return c != null && notBlank(c.getName()) ? c.getName() : "Customer";
    }

    /**
     * Meta expects an international number with no '+'. Indian shop numbers are
     * usually stored as 10 digits, so prepend the country code when needed.
     */
    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            digits = "91" + digits;
        }
        return digits;
    }

    private String money(BigDecimal v) {
        return v == null ? "0" : v.stripTrailingZeros().toPlainString();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
