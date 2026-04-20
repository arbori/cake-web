package loan.capture;

import java.math.BigDecimal;
import java.util.Objects;

public class CustomerRequest {
    private String name;
    private BigDecimal salary;
    private Long addressId;

    public String getName() {
        return name;
    }

    public CustomerRequest setName(String name) {
        this.name = name;
        return this;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public CustomerRequest setSalary(BigDecimal salary) {
        this.salary = salary;
        return this;
    }

    public Long getAddressId() {
        return addressId;
    }

    public CustomerRequest setAddressId(Long addressId) {
        this.addressId = addressId;
        return this;
    }

    @Override
    public String toString() {
        return "'CustomerData': {'name'='" + name + "', 'salary'=" + salary + ", 'addressId'=" + addressId +"}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, salary, addressId);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
