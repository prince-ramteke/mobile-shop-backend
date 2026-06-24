package com.shopmanager.service.impl;

import com.shopmanager.entity.Customer;
import com.shopmanager.exception.ResourceNotFoundException;
import java.util.List;
import com.shopmanager.dto.customer.CustomerRequest;
import com.shopmanager.dto.customer.CustomerResponse;
import com.shopmanager.entity.Customer;
import com.shopmanager.exception.DuplicateEntryException;
import com.shopmanager.exception.ResourceNotFoundException;
import com.shopmanager.mapper.CustomerMapper;
import com.shopmanager.repository.CustomerRepository;
import com.shopmanager.repository.RepairJobRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final SaleRepository saleRepository;
    private final RepairJobRepository repairJobRepository;

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateEntryException("Phone number already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerRepository.save(customer);

        CustomerResponse res = customerMapper.toResponse(saved);

// ---- ADD THIS ----
        res.setDueAmount(BigDecimal.ZERO);
// ------------------

        return res;

    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!customer.getPhone().equals(request.getPhone())
                && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateEntryException("Phone number already exists");
        }

        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setWhatsappNumber(
                request.getWhatsappNumber() != null ? request.getWhatsappNumber() : request.getPhone()
        );
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        Customer saved = customerRepository.save(customer);
        CustomerResponse res = customerMapper.toResponse(saved);

// ---- ADD THIS ----
        BigDecimal saleDue = saleRepository.sumPendingByCustomerId(saved.getId());
        BigDecimal repairDue = repairJobRepository.sumPendingByCustomerId(saved.getId());

        if (saleDue == null) saleDue = BigDecimal.ZERO;
        if (repairDue == null) repairDue = BigDecimal.ZERO;

        res.setDueAmount(saleDue.add(repairDue));
// ------------------

        return res;

    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        CustomerResponse res = customerMapper.toResponse(customer);

        BigDecimal saleDue = saleRepository.sumPendingByCustomerId(customer.getId());
        BigDecimal repairDue = repairJobRepository.sumPendingByCustomerId(customer.getId());

        if (saleDue == null) saleDue = BigDecimal.ZERO;
        if (repairDue == null) repairDue = BigDecimal.ZERO;

        res.setDueAmount(saleDue.add(repairDue));

        return res;
    }



    @Override
    public Page<CustomerResponse> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (query == null || query.trim().isEmpty()) {
            return customerRepository.findAllOrderByCreatedAtDesc(pageable)
                    .map(customer -> {
                        CustomerResponse res = customerMapper.toResponse(customer);

                        BigDecimal saleDue = saleRepository.sumPendingByCustomerId(customer.getId());
                        BigDecimal repairDue = repairJobRepository.sumPendingByCustomerId(customer.getId());

                        if (saleDue == null) saleDue = BigDecimal.ZERO;
                        if (repairDue == null) repairDue = BigDecimal.ZERO;

                        res.setDueAmount(saleDue.add(repairDue));

                        return res;
                    });

        }

        Page<Customer> customers = customerRepository.searchCustomers(query, pageable);
        return customers.map(customer -> {
            CustomerResponse res = customerMapper.toResponse(customer);

            BigDecimal saleDue = saleRepository.sumPendingByCustomerId(customer.getId());
            BigDecimal repairDue = repairJobRepository.sumPendingByCustomerId(customer.getId());

            if (saleDue == null) saleDue = BigDecimal.ZERO;
            if (repairDue == null) repairDue = BigDecimal.ZERO;

            res.setDueAmount(saleDue.add(repairDue));

            return res;
        });

    }


    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id))
            throw new ResourceNotFoundException("Customer not found");
        customerRepository.deleteById(id);
    }

    @Override
    public Customer getEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(customer -> {

                    CustomerResponse res = customerMapper.toResponse(customer);

                    BigDecimal saleDue =
                            saleRepository.sumPendingByCustomerId(customer.getId());

                    BigDecimal repairDue =
                            repairJobRepository.sumPendingByCustomerId(customer.getId());

                    if (saleDue == null) saleDue = BigDecimal.ZERO;
                    if (repairDue == null) repairDue = BigDecimal.ZERO;

                    res.setDueAmount(saleDue.add(repairDue));

                    return res;
                })
                .toList();
    }

}