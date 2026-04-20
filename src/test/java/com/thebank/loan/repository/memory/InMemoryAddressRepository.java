package com.thebank.loan.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.thebank.loan.entity.AddressEntity;
import com.thebank.loan.repository.AddressRepository;
import com.thebank.loan.repository.Repository;

public class InMemoryAddressRepository implements Repository<AddressEntity, Long>, AddressRepository {
    private final Map<Long, AddressEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public AddressEntity save(AddressEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<AddressEntity> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<AddressEntity> findAll() {
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
