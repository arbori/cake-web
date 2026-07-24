package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.AddressEntity;

public interface AddressRepository {

    AddressEntity save(AddressEntity entity);

    List<AddressEntity> saveAll(List<AddressEntity> entities);

    Optional<AddressEntity> findById(Integer id);

    List<AddressEntity> findAll();

    Optional<AddressEntity> deleteById(Integer id);

    boolean existsById(Integer id);

}