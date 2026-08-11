package com.thebank.loan.repository;

import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.InstallmentEntity;
import com.thebank.loan.entity.ProposalEntity;

public interface InstallmentRepository {

    InstallmentEntity save(InstallmentEntity entity);

    Optional<InstallmentEntity> findById(Integer id);

    List<InstallmentEntity> findAll();

    Optional<InstallmentEntity> deleteById(Integer id);

    boolean existsById(Integer id);

    List<InstallmentEntity> findByLoanRequestId(Integer loanRequestId);

    List<InstallmentEntity> findByCustomerId(Integer customerId, List<ProposalEntity> customerLoans);

}