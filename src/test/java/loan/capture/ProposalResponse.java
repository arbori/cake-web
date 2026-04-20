package loan.capture;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProposalResponse {
    private Long id;
    private Long customerId;
    private BigDecimal amount;
    private Integer numberOfInstallments;
    private BigDecimal monthlyInterestRate; // ex: 0.02 para 2% ao mês
    private LocalDate requestDate;
    private List<InstallmentResponse> installments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public ProposalResponse setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public ProposalResponse setCustomerId(Long customerId) {
        this.customerId = customerId;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ProposalResponse setAmount(BigDecimal amount) {
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

    public BigDecimal getMonthlyInterestRate() {
        return monthlyInterestRate;
    }

    public ProposalResponse setMonthlyInterestRate(BigDecimal monthlyInterestRate) {
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
