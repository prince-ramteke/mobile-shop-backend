package com.shopmanager.service;
import java.util.List;
import com.shopmanager.dto.customer.CustomerRequest;
import com.shopmanager.dto.customer.CustomerResponse;
import com.shopmanager.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    List<CustomerResponse> getAllCustomers();

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    CustomerResponse getCustomerById(Long id);

    Page<CustomerResponse> search(String query, int page, int size);

    void deleteCustomer(Long id);

    Customer getEntityById(Long id);

}