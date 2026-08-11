package com.thebank.loan.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.thebank.loan.entity.ProposalEntity;
import com.thebank.loan.entity.ProposalStatus;
import com.thebank.loan.repository.ProposalRepository;
import com.thebank.loan.repository.Repository;

public class InMemoryProposalRepository implements Repository<ProposalEntity, Integer>, ProposalRepository {
    private final Map<Integer, ProposalEntity> store = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private static InMemoryProposalRepository instance;

    private InMemoryProposalRepository() {
        // Private constructor to prevent instantiation
    }

    public static synchronized InMemoryProposalRepository instance() {
        if (instance == null) {
            instance = new InMemoryProposalRepository();
        }
        return instance;
    }

    @Override
    public ProposalEntity save(ProposalEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
            entity.setStatus(ProposalStatus.PENDING);
        }

        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public List<ProposalEntity> saveAll(List<ProposalEntity> proposalList) {
        proposalList.forEach(this::save);
        return proposalList;
    }

    @Override
    public Optional<ProposalEntity> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ProposalEntity> findByCustomerId(Integer customerId) {
        return store.values().stream()
            .filter(proposal -> proposal.getCustomerId().equals(customerId))
            .toList();
    }

    @Override
    public Optional<ProposalEntity> findByIdAndCustomerId(Integer customerId, Integer proposalId) {
        return Optional.ofNullable(store.values().stream()
                .filter(proposal -> proposal.getCustomerId().equals(customerId) && proposal.getId().equals(proposalId))
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<ProposalEntity> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<ProposalEntity> deleteById(Integer id) {
        if(id != null) {
            ProposalEntity deleted = store.get(id);

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

