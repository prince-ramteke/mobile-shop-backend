package com.shopmanager.settings.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopSettingsRequest {

    @NotBlank
    private String shopName;
    private String shopPhone;
    private String shopAddress;
    private String gstNumber;
    private String email;
    private String invoiceFooter;
    @NotNull
    @Min(0)
    @Max(100)
    private Double gstPercentage;
    private Boolean whatsappEnabled;

    private Integer reminderGapDays;
}