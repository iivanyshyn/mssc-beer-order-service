package com.ivn.msscbeerorderservice.web.mappers;

import com.ivn.brewery.model.CustomerDto;
import com.ivn.msscbeerorderservice.domain.Customer;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    CustomerDto customerToDto(Customer customer);

    Customer dtoToCustomer(CustomerDto dto);
}
