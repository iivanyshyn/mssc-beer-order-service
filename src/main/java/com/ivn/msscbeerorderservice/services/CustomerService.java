package com.ivn.msscbeerorderservice.services;

import com.ivn.brewery.model.CustomerPagedList;
import org.springframework.data.domain.PageRequest;

public interface CustomerService {

    CustomerPagedList listCustomers(PageRequest pageable);
}
