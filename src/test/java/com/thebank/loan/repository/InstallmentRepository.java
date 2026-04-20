package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.InstallmentEntity;
import com.thebank.loan.entity.ProposalEntity;

public interface InstallmentRepository {

    InstallmentEntity save(InstallmentEntity entity);

    Optional<InstallmentEntity> findById(Long id);

    List<InstallmentEntity> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    List<InstallmentEntity> findByLoanRequestId(Long loanRequestId);

    List<InstallmentEntity> findByCustomerId(Long customerId, List<ProposalEntity> customerLoans);

}