package com.thebank.loan.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.thebank.loan.entity.CustomerEntity;
import com.thebank.loan.repository.CustomerRepository;
import com.thebank.loan.repository.Repository;

public class InMemoryCustomerRepository implements Repository<CustomerEntity, Long>, CustomerRepository {
    private final Map<Long, CustomerEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public CustomerEntity save(CustomerEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<CustomerEntity> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<CustomerEntity> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
