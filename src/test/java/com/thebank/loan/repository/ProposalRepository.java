package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.ProposalEntity;

public interface ProposalRepository {

    ProposalEntity save(ProposalEntity entity);

    List<ProposalEntity> saveAll(List<ProposalEntity> asList);

    Optional<ProposalEntity> findById(Integer id);

    List<ProposalEntity> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);
}