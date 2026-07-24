package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.ProposalEntity;

public interface ProposalRepository {

    ProposalEntity save(ProposalEntity entity);

    List<ProposalEntity> saveAll(List<ProposalEntity> asList);

    Optional<ProposalEntity> findById(Integer id);

    List<ProposalEntity> findByCustomerId(Integer customerId);

    Optional<ProposalEntity> findByIdAndCustomerId(Integer customerId, Integer proposalId);

    List<ProposalEntity> findAll();

    Optional<ProposalEntity> deleteById(Integer id);

    boolean existsById(Integer id);
}