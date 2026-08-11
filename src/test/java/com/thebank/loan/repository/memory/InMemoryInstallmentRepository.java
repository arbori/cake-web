package com.thebank.loan.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.thebank.loan.entity.InstallmentEntity;
import com.thebank.loan.entity.ProposalEntity;
import com.thebank.loan.repository.InstallmentRepository;
import com.thebank.loan.repository.Repository;

public class InMemoryInstallmentRepository implements Repository<InstallmentEntity, Integer>, InstallmentRepository {
    private final Map<Integer, InstallmentEntity> store = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private static InMemoryInstallmentRepository instance;

    private InMemoryInstallmentRepository() {
        // Private constructor to prevent instantiation
    }

    public static synchronized InMemoryInstallmentRepository instance() {
        if (instance == null) {
            instance = new InMemoryInstallmentRepository();
        }
        return instance;
    }

    @Override
    public InstallmentEntity save(InstallmentEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<InstallmentEntity> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<InstallmentEntity> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<InstallmentEntity> deleteById(Integer id) {
        if(id != null) {
            InstallmentEntity deleted = store.get(id);

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

    @Override
    public List<InstallmentEntity> findByLoanRequestId(Integer loanRequestId) {
        return store.values().stream()
                .filter(i -> i.getLoanRequestId().equals(loanRequestId))
                .toList();
    }

    @Override
    public List<InstallmentEntity> findByCustomerId(Integer customerId, List<ProposalEntity> customerLoans) {
        Set<Integer> loanIds = customerLoans.stream()
            .filter(req -> req.getCustomerId() == customerId)
            .map(ProposalEntity::getId)
            .collect(Collectors.toSet());
        
        return store.values().stream()
                .filter(i -> loanIds.contains(i.getLoanRequestId()))
                .toList();
    }
}

