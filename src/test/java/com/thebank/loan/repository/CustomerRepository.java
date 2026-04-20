package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.CustomerEntity;

public interface CustomerRepository {

    CustomerEntity save(CustomerEntity entity);

    Optional<CustomerEntity> findById(Long id);

    List<CustomerEntity> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

}