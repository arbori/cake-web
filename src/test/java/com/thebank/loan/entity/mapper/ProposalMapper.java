package com.thebank.loan.entity.mapper;

import com.thebank.loan.entity.ProposalEntity;
import com.thebank.loan.model.ProposalResponse;

public class ProposalMapper {
    public static ProposalResponse toProposalResponse(ProposalEntity proposalEntity) {
        return new ProposalResponse()
            .setId(proposalEntity.getId())
            .setCustomerId(proposalEntity.getCustomerId())
            .setAmount(proposalEntity.getAmount())
            .setInstallments(proposalEntity.getInstallments().stream().map(MapperInstallment::toInstallmentResponse).toList());
    }
}
