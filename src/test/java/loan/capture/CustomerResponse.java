package loan.capture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerResponse {
    private Integer id;
    private String name;
    private Double salary;
    private Integer addressId;
    private List<ProposalResponse> loanRequests = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public CustomerResponse setId(Integer id) {
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

    public Double getSalary() {
        return salary;
    }

    public CustomerResponse setSalary(Double salary) {
        this.salary = salary;
        return this;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public CustomerResponse setAddressId(Integer addressId) {
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
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        hash = 31 * hash + Objects.hashCode(this.name);
        hash = 31 * hash + Objects.hashCode(this.salary);
        hash = 31 * hash + Objects.hashCode(this.addressId);
        hash = 31 * hash + Objects.hashCode(this.loanRequests);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CustomerResponse other = (CustomerResponse) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.salary, other.salary)) {
            return false;
        }
        if (!Objects.equals(this.addressId, other.addressId)) {
            return false;
        }
        if (!Objects.equals(this.loanRequests, other.loanRequests)) {
            return false;
        }
        return true;
    }
}
