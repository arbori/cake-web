package loan.capture;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class InstallmentResponse {
    private Long id;
    private Long loanRequestId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amount;
    private LocalDate paidDate;
    private Boolean isLate;

    public Long getId() {
        return id;
    }

    public InstallmentResponse setId(Long id) {
        this.id = id;
        return this;
    }
    
    public Long getLoanRequestId() {
        return loanRequestId;
    }

    public InstallmentResponse setLoanRequestId(Long loanRequestId) {
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

    public BigDecimal getAmount() {
        return amount;
    }

    public InstallmentResponse setAmount(BigDecimal amount) {
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
