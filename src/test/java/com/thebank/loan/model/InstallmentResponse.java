package com.thebank.loan.model;

import java.time.LocalDate;
import java.util.Objects;

public class InstallmentResponse {
    private Integer id;
    private Integer loanRequestId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private Double amount;
    private LocalDate paidDate;
    private Boolean isLate;

    public Integer getId() {
        return id;
    }

    public InstallmentResponse setId(Integer id) {
        this.id = id;
        return this;
    }
    
    public Integer getLoanRequestId() {
        return loanRequestId;
    }

    public InstallmentResponse setLoanRequestId(Integer loanRequestId) {
        this.loanRequestId = loanRequestId;
        return this;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public InstallmentResponse setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
        return this;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public InstallmentResponse setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public InstallmentResponse setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public InstallmentResponse setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
        return this;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public InstallmentResponse setIsLate(Boolean isLate) {
        this.isLate = isLate;
        return this;
    }

    @Override
    public String toString() {
        return "'InstallmentData': {'id'=" + id + ", 'loanRequestId'=" + loanRequestId + ", 'installmentNumber'=" + installmentNumber + ", 'dueDate'=" + dueDate + ", 'amount'=" + amount + ", 'paidDate'=" + paidDate + ", 'isLate'=" + isLate + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, loanRequestId, installmentNumber, dueDate, amount, paidDate, isLate);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
