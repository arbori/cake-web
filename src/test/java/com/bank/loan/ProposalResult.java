package com.bank.loan;

public record ProposalResult(Integer proposalId, CustomerResult customer, Double amount, String status) {
}
