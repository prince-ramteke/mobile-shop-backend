package com.shopmanager.service.impl;

import com.shopmanager.dto.report.DailyReportDto;
import com.shopmanager.dto.report.MonthlyReportDto;
import com.shopmanager.service.ReportPdfService;
import com.shopmanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportPdfServiceImpl implements ReportPdfService {

    private final ReportService reportService;
    private final TemplateEngine templateEngine;

    @Override
    public byte[] exportDailyPdf(String date) {

        DailyReportDto report = reportService.getDailyReport(date);

        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("generatedAt", LocalDateTime.now());

        String html = templateEngine.process("daily-report", context);

        return generatePdfFromHtml(html);
    }

    @Override
    public byte[] exportMonthlyPdf(int year, int month) {

        MonthlyReportDto report =
                reportService.getMonthlyReport(year, month);

        // Chart image (JFreeChart) removed to save memory on the free tier.
        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("generatedAt", LocalDateTime.now());
        context.setVariable("chartImage", null);

        String html = templateEngine.process("monthly-report", context);
        return generatePdfFromHtml(html);
    }

    private byte[] generatePdfFromHtml(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}