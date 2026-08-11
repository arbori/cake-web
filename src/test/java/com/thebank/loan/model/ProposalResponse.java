package com.thebank.loan.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProposalResponse {
    private Integer id;
    private Integer customerId;
    private Double amount;
    private Integer numberOfInstallments;
    private Double monthlyInterestRate; // ex: 0.02 para 2% ao mês
    private LocalDate requestDate;
    private List<InstallmentResponse> installments = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public ProposalResponse setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public ProposalResponse setCustomerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public ProposalResponse setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public ProposalResponse setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
        return this;
    }

    public Double getMonthlyInterestRate() {
        return monthlyInterestRate;
    }

    public ProposalResponse setMonthlyInterestRate(Double monthlyInterestRate) {
        this.monthlyInterestRate = monthlyInterestRate;
        return this;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public ProposalResponse setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
        return this;
    }

    public List<InstallmentResponse> getInstallments() {
        return installments;
    }

    public ProposalResponse setInstallments(List<InstallmentResponse> installments) {
        this.installments = installments;
        return this;
    }

    @Override
    public String toString() {
        return "'ProposalResponse': {'id'='" + id + "', 'customerId'='" + customerId + "', 'amount'=" + amount + ", 'numberOfInstallments'=" + numberOfInstallments
                + ", 'monthlyInterestRate'=" + monthlyInterestRate + ", 'requestDate'=" + requestDate + ", 'installments'=" + installments + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, amount, numberOfInstallments, monthlyInterestRate, requestDate, installments);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
