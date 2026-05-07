package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.CustomerEntity;

public interface CustomerRepository {

    CustomerEntity save(CustomerEntity entity);

    List<CustomerEntity> saveAll(List<CustomerEntity> entities);

    Optional<CustomerEntity> findById(Integer id);

    List<CustomerEntity> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);

}