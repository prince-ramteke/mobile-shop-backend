package com.shopmanager.service.impl;

import com.shopmanager.entity.RepairJob;
import com.shopmanager.repository.RepairPaymentRepository;
import com.shopmanager.service.RepairInvoiceService;
import com.shopmanager.settings.entity.ShopSettings;
import com.shopmanager.settings.service.SettingsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairInvoiceServiceImpl implements RepairInvoiceService {

    private final RepairPaymentRepository repairPaymentRepository;

    private final TemplateEngine templateEngine;

    private final SettingsProvider settingsProvider;

    @Override
    public byte[] generateRepairInvoicePdf(RepairJob job) {

        try {
            Context context = new Context();

            // Shop Info (from Settings)
            ShopSettings shop = settingsProvider.getSettings();
            context.setVariable("shopName",
                    (shop.getShopName() != null && !shop.getShopName().isBlank())
                            ? shop.getShopName() : "Saurabh Mobile Shop");
            context.setVariable("shopAddress",
                    shop.getShopAddress() != null ? shop.getShopAddress() : "");
            context.setVariable("shopPhone",
                    shop.getShopPhone() != null ? shop.getShopPhone() : "");
            context.setVariable("gstNumber",
                    (shop.getGstNumber() != null && !shop.getGstNumber().isBlank())
                            ? "GSTIN: " + shop.getGstNumber() : "");

            context.setVariable("invoiceNumber", job.getJobNumber());
            context.setVariable("date",
                    job.getCreatedAt() != null
                            ? job.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            : "");

            context.setVariable("customer", job.getCustomer());

            // Fake item row for repair (device + issue)
            context.setVariable("items", List.of(
                    new Object() {
                        public String getItemName() {
                            return job.getDeviceBrand() + " " + job.getDeviceModel()
                                    + " (" + job.getIssueDescription() + ")";
                        }

                        public int getQuantity() { return 1; }

                        public Object getUnitPrice() { return job.getFinalCost(); }

                        public Object getLineTotal() { return job.getFinalCost(); }
                    }
            ));

            context.setVariable("subTotal", job.getFinalCost());
            context.setVariable("cgstAmount", 0);
            context.setVariable("sgstAmount", 0);
            context.setVariable("igstAmount", 0);
            context.setVariable("grandTotal", job.getFinalCost());
            context.setVariable("amountReceived", job.getAdvancePaid());
            context.setVariable("pendingAmount", job.getPendingAmount());

            context.setVariable("paymentMode", "Cash");
            context.setVariable("notes", job.getIssueDescription());
            context.setVariable("deviceBrand", job.getDeviceBrand());
            context.setVariable("deviceModel", job.getDeviceModel());
            context.setVariable("imei", job.getImei());
            context.setVariable("estimatedCost", job.getEstimatedCost());
            context.setVariable("advancePaid", job.getAdvancePaid());


            var payments = repairPaymentRepository
                    .findByRepairJobIdOrderByPaidAtAsc(job.getId());



            java.math.BigDecimal balance =
                    job.getFinalCost() == null ? java.math.BigDecimal.ZERO : job.getFinalCost();

            java.util.List<java.util.Map<String, Object>> ledger = new java.util.ArrayList<>();

            for (var p : payments) {

                if (p == null) continue;

                java.math.BigDecimal amt =
                        p.getAmount() == null ? java.math.BigDecimal.ZERO : p.getAmount();

                balance = balance.subtract(amt);

                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("paidAt", p.getPaidAt());
                row.put("amount", amt);
                row.put("note", p.getNote() == null ? "" : p.getNote());
                row.put("balance", balance);

                ledger.add(row);
            }


            context.setVariable("payments", ledger);


            String html = templateEngine.process("repair_receipt", context);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate repair invoice", e);
        }
    }
}