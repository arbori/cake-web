package com.thebank.loan.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.thebank.loan.entity.InstallmentEntity;
import com.thebank.loan.entity.ProposalEntity;
import com.thebank.loan.repository.InstallmentRepository;
import com.thebank.loan.repository.Repository;

public class InMemoryInstallmentRepository implements Repository<InstallmentEntity, Long>, InstallmentRepository {
    private final Map<Long, InstallmentEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public InstallmentEntity save(InstallmentEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<InstallmentEntity> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<InstallmentEntity> findAll() {
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

    @Override
    public List<InstallmentEntity> findByLoanRequestId(Long loanRequestId) {
        return store.values().stream()
                .filter(i -> i.getLoanRequestId().equals(loanRequestId))
                .toList();
    }

    @Override
    public List<InstallmentEntity> findByCustomerId(Long customerId, List<ProposalEntity> customerLoans) {
        Set<Long> loanIds = customerLoans.stream()
            .filter(req -> req.getCustomerId() == customerId)
            .map(ProposalEntity::getId)
            .collect(Collectors.toSet());
        
        return store.values().stream()
                .filter(i -> loanIds.contains(i.getLoanRequestId()))
                .toList();
    }
}

