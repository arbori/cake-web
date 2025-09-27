package com.bank.loan;

public record ProposalResult(String proposalId, CustomerResult customer, double amount, String status) {
}
