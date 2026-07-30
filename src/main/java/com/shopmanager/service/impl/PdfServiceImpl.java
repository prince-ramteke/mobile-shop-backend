package com.shopmanager.service.impl;

import com.shopmanager.entity.MobileSale;
import com.shopmanager.entity.RepairJob;
import com.shopmanager.entity.Sale;
import com.shopmanager.exception.ResourceNotFoundException;
import com.shopmanager.repository.MobileSaleRepository;
import com.shopmanager.repository.RepairJobRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.service.PdfService;
import com.shopmanager.settings.entity.ShopSettings;
import com.shopmanager.settings.service.SettingsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.shopmanager.entity.Customer;
import com.shopmanager.repository.CustomerRepository;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {
    private final CustomerRepository customerRepository;

    private final MobileSaleRepository mobileSaleRepository;

    private final SaleRepository saleRepository;
    private final RepairJobRepository repairJobRepository;
    private final SpringTemplateEngine templateEngine;
    private final SettingsProvider settingsProvider;

    /** Fills shopName/shopAddress/shopPhone into the template from Settings. */
    private void applyShopInfo(Context ctx) {
        ShopSettings shop = settingsProvider.getSettings();
        ctx.setVariable("shopName", nz(shop.getShopName(), "Saurabh Mobile Shop"));
        ctx.setVariable("shopAddress", nz(shop.getShopAddress(), ""));
        ctx.setVariable("shopPhone", nz(shop.getShopPhone(), ""));
        ctx.setVariable("gstNumber", gstLabel(shop.getGstNumber()));
    }

    private static String nz(String v, String def) {
        return (v != null && !v.isBlank()) ? v : def;
    }

    private static String gstLabel(String g) {
        return (g != null && !g.isBlank()) ? "GSTIN: " + g : "";
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMobileSaleInvoicePdf(Long id) {

        MobileSale sale = mobileSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mobile sale not found"));

        Context ctx = new Context();

        // Shop Info (from Settings)
        applyShopInfo(ctx);
        ctx.setVariable("paymentMode", "CASH");

        // Sale Info
        ctx.setVariable("invoiceNumber", String.format("MS-%04d", sale.getId()));
        ctx.setVariable("date", sale.getCreatedAt().toLocalDate());

        ctx.setVariable("company", sale.getCompany());
        ctx.setVariable("model", sale.getModel());
        ctx.setVariable("imei1", sale.getImei1());
        ctx.setVariable("imei2", sale.getImei2());
        ctx.setVariable("qty", sale.getQuantity());

        ctx.setVariable("price", sale.getPrice());
        ctx.setVariable("total", sale.getGrandTotal());
        ctx.setVariable("advance", sale.getAdvancePaid());
        ctx.setVariable("pending", sale.getPendingAmount());

        // ================= EMI VARIABLES =================
        ctx.setVariable("emiEnabled", sale.getEmiEnabled());
        ctx.setVariable("emiMonths", sale.getEmiMonths());
        ctx.setVariable("emiInterestRate", sale.getEmiInterestRate());
        ctx.setVariable("emiProcessingFee", sale.getEmiProcessingFee());
        ctx.setVariable("emiInterestAmount", sale.getEmiInterestAmount());
        ctx.setVariable("emiTotalPayable", sale.getEmiTotalPayable());
        ctx.setVariable("emiMonthlyAmount", sale.getEmiMonthlyAmount());


        ctx.setVariable("warrantyYears", sale.getWarrantyYears());
        ctx.setVariable("warrantyExpiry", sale.getWarrantyExpiry());
        // Customer Info
        Customer customer = customerRepository.findById(sale.getCustomerId())
                .orElse(null);

        if (customer != null) {
            ctx.setVariable("customerName", customer.getName());
            ctx.setVariable("customerPhone", customer.getPhone());
            ctx.setVariable("customerAddress", customer.getAddress());
        } else {
            ctx.setVariable("customerName", "-");
            ctx.setVariable("customerPhone", "-");
            ctx.setVariable("customerAddress", "-");
        }



        String html = templateEngine.process("mobile-sale-invoice", ctx);
        return renderPdf(html);
    }

    @Override
    @Transactional(readOnly = true)  // ADD THIS for lazy loading
    public byte[] generateSaleInvoicePdf(Long saleId) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        // Force initialization of lazy collections
        if (sale.getCustomer() != null) {
            sale.getCustomer().getName(); // Force load
        }
        if (sale.getItems() != null) {
            sale.getItems().size(); // Force load
        }

        Context ctx = new Context();

        // Shop info (from Settings)
        applyShopInfo(ctx);

        // Sale info
        ctx.setVariable("invoiceNumber", sale.getInvoiceNumber());
        ctx.setVariable("date", sale.getSaleDate());
        ctx.setVariable("customer", sale.getCustomer());
        ctx.setVariable("items", sale.getItems());

        // Amounts
        ctx.setVariable("subTotal", sale.getSubTotal());
        ctx.setVariable("cgstAmount", sale.getCgstAmount());
        ctx.setVariable("sgstAmount", sale.getSgstAmount());
        ctx.setVariable("igstAmount", sale.getIgstAmount());
        ctx.setVariable("totalTax", sale.getTotalTax());
        ctx.setVariable("grandTotal", sale.getGrandTotal());
        ctx.setVariable("amountReceived", sale.getAmountReceived());
        ctx.setVariable("pendingAmount", sale.getPendingAmount());

        ctx.setVariable("paymentMode", sale.getPaymentMode());
        ctx.setVariable("notes", sale.getNotes());

        String html = templateEngine.process("sale_invoice", ctx);
        return renderPdf(html);

    }

    @Override
    @Transactional(readOnly = true)  // ADD THIS
    public byte[] generateRepairReceiptPdf(Long repairJobId) {

        RepairJob job = repairJobRepository.findById(repairJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair job not found"));

        // Force initialization
        if (job.getCustomer() != null) {
            job.getCustomer().getName();
        }

        Context ctx = new Context();
        ctx.setVariable("job", job);
        applyShopInfo(ctx);

        String html = templateEngine.process("repair_receipt", ctx);

        return renderPdf(html);
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }
}