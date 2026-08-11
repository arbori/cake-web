package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, I> {
    T save(T entity);
    Optional<T> findById(I id);
    List<T> findAll();
    Optional<T> deleteById(I id);
    boolean existsById(I id);
}
