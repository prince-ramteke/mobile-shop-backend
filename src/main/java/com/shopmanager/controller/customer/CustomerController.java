package com.shopmanager.controller.customer;

import com.shopmanager.repository.SaleRepository;
import com.shopmanager.repository.RepairJobRepository;
import com.shopmanager.entity.Customer;
import com.shopmanager.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.shopmanager.dto.customer.CustomerRequest;
import com.shopmanager.dto.customer.CustomerResponse;
import com.shopmanager.service.CustomerLedgerService;
import com.shopmanager.service.CustomerReminderService;
import com.shopmanager.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerReminderService customerReminderService;

    private final CustomerLedgerService customerLedgerService;


    private final CustomerService customerService;
    private final SaleRepository saleRepository;
    private final RepairJobRepository repairJobRepository;

    @GetMapping("/{id}/ledger")
    public ResponseEntity<?> getLedger(@PathVariable Long id) {
        return ResponseEntity.ok(customerLedgerService.getLedger(id));
    }


    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(customerService.search(query, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @RequestBody CustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }

    // ================= CUSTOMER FINANCIAL SUMMARY =================
    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getCustomerSummary(@PathVariable Long id) {

        Customer customer = customerService.getEntityById(id);

        BigDecimal salePending = saleRepository.sumPendingByCustomerId(id);
        BigDecimal repairPending = repairJobRepository.sumPendingByCustomerId(id);

        if (salePending == null) salePending = BigDecimal.ZERO;
        if (repairPending == null) repairPending = BigDecimal.ZERO;

        BigDecimal totalDue = salePending.add(repairPending);

        Map<String, Object> res = new HashMap<>();
        res.put("salePending", salePending);
        res.put("repairPending", repairPending);
        res.put("totalDue", totalDue);

        return ResponseEntity.ok(res);
    }

    // ================= CUSTOMER FULL PROFILE =================
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getCustomerProfile(@PathVariable Long id) {

        Customer customer = customerService.getEntityById(id);

        BigDecimal salePending = saleRepository.sumPendingByCustomerId(id);
        BigDecimal repairPending = repairJobRepository.sumPendingByCustomerId(id);

        if (salePending == null) salePending = BigDecimal.ZERO;
        if (repairPending == null) repairPending = BigDecimal.ZERO;

        BigDecimal totalPending = salePending.add(repairPending);

        BigDecimal totalSales = saleRepository.sumTotalByCustomerId(id);
        BigDecimal totalRepairs = repairJobRepository.sumTotalByCustomerId(id);

        if (totalSales == null) totalSales = BigDecimal.ZERO;
        if (totalRepairs == null) totalRepairs = BigDecimal.ZERO;

        BigDecimal totalBusiness = totalSales.add(totalRepairs);
        BigDecimal totalPaid = totalBusiness.subtract(totalPending);

        Map<String, Object> res = new HashMap<>();

        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("id", customer.getId());
        customerMap.put("name", customer.getName());
        customerMap.put("phone", customer.getPhone());
        customerMap.put("email", customer.getEmail());
        customerMap.put("address", customer.getAddress());

        res.put("customer", customerMap);

        res.put("totalBusiness", totalBusiness);
        res.put("totalPaid", totalPaid);
        res.put("totalPending", totalPending);
        res.put("salePending", salePending);
        res.put("repairPending", repairPending);

        res.put("sales", saleRepository.findByCustomerIdOrderBySaleDateDesc(id));
        res.put("repairs", repairJobRepository.findByCustomerIdOrderByCreatedAtDesc(id));

        return ResponseEntity.ok(res);
    }

    // ================= CUSTOMER DASHBOARD =================
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<?> getCustomerDashboard(@PathVariable Long id) {

        Customer customer = customerService.getEntityById(id);

        BigDecimal totalSales = saleRepository.sumTotalByCustomerId(id);
        BigDecimal totalRepairs = repairJobRepository.sumTotalByCustomerId(id);

        if (totalSales == null) totalSales = BigDecimal.ZERO;
        if (totalRepairs == null) totalRepairs = BigDecimal.ZERO;

        BigDecimal totalBusiness = totalSales.add(totalRepairs);

        BigDecimal salePending = saleRepository.sumPendingByCustomerId(id);
        BigDecimal repairPending = repairJobRepository.sumPendingByCustomerId(id);

        if (salePending == null) salePending = BigDecimal.ZERO;
        if (repairPending == null) repairPending = BigDecimal.ZERO;

        BigDecimal totalPending = salePending.add(repairPending);
        BigDecimal totalPaid = totalBusiness.subtract(totalPending);

        Long salesCount = saleRepository.countByCustomerId(id);
        Long repairsCount = repairJobRepository.countByCustomerId(id);

        Object lastSaleDate = saleRepository.findLastSaleDate(id);
        Object lastRepairDate = repairJobRepository.findLastRepairDate(id);

        Object lastVisit = (lastRepairDate != null) ? lastRepairDate : lastSaleDate;

        Map<String, Object> res = new HashMap<>();

        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("id", customer.getId());
        customerMap.put("name", customer.getName());
        customerMap.put("phone", customer.getPhone());
        customerMap.put("email", customer.getEmail());
        customerMap.put("address", customer.getAddress());

        res.put("customer", customerMap);
        res.put("totalSalesAmount", totalSales);
        res.put("totalRepairsAmount", totalRepairs);
        res.put("totalBusiness", totalBusiness);
        res.put("totalPaid", totalPaid);
        res.put("totalPending", totalPending);
        res.put("salesCount", salesCount);
        res.put("repairsCount", repairsCount);
        res.put("lastVisitDate", lastVisit);

        return ResponseEntity.ok(res);
    }

    // ================= PAYMENT REMINDERS =================
    @GetMapping("/reminders")
    public ResponseEntity<?> getPaymentReminders() {
        return ResponseEntity.ok(customerReminderService.getPendingReminders());
    }


}