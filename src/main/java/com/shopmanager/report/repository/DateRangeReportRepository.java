package com.shopmanager.report.repository;

import com.shopmanager.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DateRangeReportRepository extends JpaRepository<Sale, Long> {

    // Sale.saleDate is a LocalDate, so the bound parameters must also be
    // LocalDate. Binding LocalDateTime here previously caused a 500
    // (InvalidDataAccessApiUsageException: type mismatch) on every request.
    @Query("""
        SELECT
            COUNT(s.id),
            COALESCE(SUM(s.subTotal), 0),
            COALESCE(SUM(s.totalTax), 0),
            COALESCE(SUM(s.grandTotal), 0),
            COALESCE(SUM(s.amountReceived), 0),
            COALESCE(SUM(s.pendingAmount), 0)
        FROM Sale s
        WHERE s.saleDate BETWEEN :start AND :end
    """)
    List<Object[]> getDateRangeSummary(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // Sales revenue grouped by day.
    @Query("""
        SELECT s.saleDate, COALESCE(SUM(s.grandTotal), 0)
        FROM Sale s
        WHERE s.saleDate BETWEEN :start AND :end
        GROUP BY s.saleDate
    """)
    List<Object[]> getDailySales(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // Repair revenue (final cost) grouped by the day the job was created.
    @Query("""
        SELECT FUNCTION('DATE', r.createdAt), COALESCE(SUM(r.finalCost), 0)
        FROM RepairJob r
        WHERE FUNCTION('DATE', r.createdAt) BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE', r.createdAt)
    """)
    List<Object[]> getDailyRepairs(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // Count of repair jobs created within the range.
    @Query("""
        SELECT COUNT(r)
        FROM RepairJob r
        WHERE FUNCTION('DATE', r.createdAt) BETWEEN :start AND :end
    """)
    long countRepairsBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // Sales totals grouped by item category (SaleItem.type).
    @Query("""
        SELECT COALESCE(si.type, 'Other'), COALESCE(SUM(si.lineTotal), 0)
        FROM SaleItem si
        WHERE si.sale.saleDate BETWEEN :start AND :end
        GROUP BY si.type
    """)
    List<Object[]> getSalesByCategory(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}