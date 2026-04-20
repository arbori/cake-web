package loan.capture;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerResponse {
    private Long id;
    private String name;
    private BigDecimal salary;
    private Long addressId;
    private List<ProposalResponse> loanRequests = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public CustomerResponse setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CustomerResponse setName(String name) {
        this.name = name;
        return this;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public CustomerResponse setSalary(BigDecimal salary) {
        this.salary = salary;
        return this;
    }

    public Long getAddressId() {
        return addressId;
    }

    public CustomerResponse setAddressId(Long addressId) {
        this.addressId = addressId;
        return this;
    }

    public List<ProposalResponse> getLoanRequests() {
        return loanRequests;
    }

    public CustomerResponse setLoanRequests(List<ProposalResponse> loanRequests) {
        this.loanRequests = loanRequests;
        return this;
    }

    @Override
    public String toString() {
        return "'CustomerData': {'id'='" + id + "', 'name'='" + name + "', 'salary'=" + salary + ", 'addressId'=" + addressId
                + ", 'loanRequests'=" + loanRequests + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, salary, addressId, loanRequests);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
