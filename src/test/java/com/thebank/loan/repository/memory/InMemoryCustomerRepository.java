package com.thebank.loan.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.thebank.loan.entity.AddressEntity;
import com.thebank.loan.entity.CustomerEntity;
import com.thebank.loan.repository.CustomerRepository;
import com.thebank.loan.repository.Repository;

public class InMemoryCustomerRepository implements Repository<CustomerEntity, Integer>, CustomerRepository {
    private final Map<Integer, CustomerEntity> store = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private static InMemoryCustomerRepository instance;

    private InMemoryCustomerRepository() {
        // Private constructor to prevent instantiation
    }

    public static synchronized InMemoryCustomerRepository instance() {
        if (instance == null) {
            instance = new InMemoryCustomerRepository();
        }
        return instance;
    }

    @Override
    public CustomerEntity save(CustomerEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public List<CustomerEntity> saveAll(List<CustomerEntity> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    public Optional<CustomerEntity> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<CustomerEntity> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<CustomerEntity> deleteById(Integer id) {
        if(id != null) {
            CustomerEntity deleted = store.get(id);

            if(deleted != null) {
                store.remove(deleted.getId());

                return Optional.of(deleted);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer id) {
        return store.containsKey(id);
    }
}
