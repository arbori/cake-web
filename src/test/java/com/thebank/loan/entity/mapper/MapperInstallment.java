package com.thebank.loan.entity.mapper;


import com.thebank.loan.entity.InstallmentEntity;
import com.thebank.loan.model.InstallmentResponse;

public class MapperInstallment {
    public static InstallmentResponse toInstallmentResponse(InstallmentEntity installmentEntity) {
        return new InstallmentResponse()
            .setId(installmentEntity.getId())
            .setLoanRequestId(installmentEntity.getLoanRequestId())
            .setInstallmentNumber(installmentEntity.getInstallmentNumber())
            .setDueDate(installmentEntity.getDueDate())
            .setAmount(installmentEntity.getAmount())
            .setPaidDate(installmentEntity.getPaidDate())
            .setIsLate(installmentEntity.getIsLate());
    }
}
