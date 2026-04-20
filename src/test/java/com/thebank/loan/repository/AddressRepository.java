package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.AddressEntity;

public interface AddressRepository {

    AddressEntity save(AddressEntity entity);

    Optional<AddressEntity> findById(Long id);

    List<AddressEntity> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

}