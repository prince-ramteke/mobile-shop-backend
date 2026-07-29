package com.shopmanager.settings.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- Shop Info ----
    @Column(nullable = false)
    private String shopName;

    private String shopPhone;
    private String shopAddress;
    private String gstNumber;
    private String email;

    // ---- Invoice ----
    private String invoiceFooter;

    // ---- Messaging Toggles ----
    @Column(nullable = false)
    private Boolean whatsappEnabled;



    // ---- Reminder ----
    @Column(nullable = false)
    private Integer reminderGapDays;

    @Column(nullable = false)
    private Double gstPercentage;
}