package com.thebank.loan.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.thebank.loan.entity.AddressEntity;
import com.thebank.loan.repository.AddressRepository;
import com.thebank.loan.repository.Repository;

public class InMemoryAddressRepository implements Repository<AddressEntity, Integer>, AddressRepository {
    private final Map<Integer, AddressEntity> store = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private static InMemoryAddressRepository instance;

    private InMemoryAddressRepository() {
        // Private constructor to prevent instantiation
    }

    public static synchronized InMemoryAddressRepository instance() {
        if (instance == null) {
            instance = new InMemoryAddressRepository();
        }
        return instance;
    }

    @Override
    public AddressEntity save(AddressEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<AddressEntity> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<AddressEntity> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Integer id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return store.containsKey(id);
    }
}
