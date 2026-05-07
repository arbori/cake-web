package com.thebank.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProposalEntity {
    private Integer id;
    private Integer customerId;
    private Double amount;
    private Integer numberOfInstallments;
    private Double monthlyInterestRate; // ex: 0.02 para 2% ao mês
    private LocalDate requestDate;
    private RiskAssessmentEntity riskAssessment;
    private List<InstallmentEntity> installments = new ArrayList<>();
    private ProposalStatus status;

    public Integer getId() {
        return id;
    }

    public ProposalEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public ProposalEntity setCustomerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public ProposalEntity setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public ProposalEntity setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
        return this;
    }

    public Double getMonthlyInterestRate() {
        return monthlyInterestRate;
    }

    public ProposalEntity setMonthlyInterestRate(Double monthlyInterestRate) {
        this.monthlyInterestRate = monthlyInterestRate;
        return this;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public ProposalEntity setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
        return this;
    }

    public RiskAssessmentEntity getRiskAssessment() {
        return riskAssessment;
    }

    public ProposalEntity setRiskAssessment(RiskAssessmentEntity riskAssessment) {
        this.riskAssessment = riskAssessment;
        return this;
    }

    public List<InstallmentEntity> getInstallments() {
        return installments;
    }

    public ProposalEntity setInstallments(List<InstallmentEntity> installments) {
        this.installments = installments;
        return this;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public ProposalEntity setStatus(ProposalStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "'ProposalEntity': {'id'='" + id + "', 'customerId'='" + customerId + "', 'amount'=" + amount + ", 'numberOfInstallments'=" + numberOfInstallments
                + ", 'monthlyInterestRate'=" + monthlyInterestRate + ", 'requestDate'=" + requestDate + ", 'riskAssessment'=" + riskAssessment
                + ", 'installments'=" + installments + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, amount, numberOfInstallments, monthlyInterestRate, requestDate, riskAssessment, installments);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
