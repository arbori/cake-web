package com.thebank.loan.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.thebank.loan.entity.AddressEntity;
import com.thebank.loan.entity.CustomerEntity;
import com.thebank.loan.entity.InstallmentEntity;
import com.thebank.loan.entity.ProposalEntity;
import com.thebank.loan.entity.RiskAssessmentEntity;
import com.thebank.loan.repository.AddressRepository;
import com.thebank.loan.repository.CustomerRepository;
import com.thebank.loan.repository.InstallmentRepository;
import com.thebank.loan.repository.ProposalRepository;

public class LoanService {
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ProposalRepository loanRequestRepository;
    private final InstallmentRepository installmentRepository;
    private final RiskAnalysisService riskAnalysisService;

    public LoanService(CustomerRepository customerRepository,
                       AddressRepository addressRepository,
                       ProposalRepository loanRequestRepository,
                       InstallmentRepository installmentRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.loanRequestRepository = loanRequestRepository;
        this.installmentRepository = installmentRepository;
        this.riskAnalysisService = new RiskAnalysisService(addressRepository, installmentRepository);
    }

    // CRUD Customer
    public CustomerEntity createCustomer(String name, BigDecimal salary, Integer addressId) {
        if (!addressRepository.existsById(addressId)) {
            throw new IllegalArgumentException("Address not found");
        }
        CustomerEntity customer = new CustomerEntity()
            .setName(name)
            .setSalary(salary)
            .setAddressId(addressId);
            
        return customerRepository.save(customer);
    }

    public Optional<CustomerEntity> getCustomer(Integer id) {
        return customerRepository.findById(id);
    }

    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    public CustomerEntity updateCustomer(Integer id, String name, BigDecimal salary, Integer addressId) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        if (name != null) customer.setName(name);
        if (salary != null) customer.setSalary(salary);
        if (addressId != null) {
            if (!addressRepository.existsById(addressId)) throw new IllegalArgumentException("Address not found");
        
            customer.setAddressId(addressId);
        }
        
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Integer id) {
        customerRepository.deleteById(id);
    }

    // CRUD Address
    public AddressEntity createAddress(String zipcode, String street, String city, String state) {
        AddressEntity address = new AddressEntity()
            .setZipcode(zipcode)
            .setStreet(street)
            .setCity(city)
            .setState(state);

        return addressRepository.save(address);
    }

    public Optional<AddressEntity> getAddress(Integer id) {
        return addressRepository.findById(id);
    }

    public List<AddressEntity> getAllAddresses() {
        return addressRepository.findAll();
    }

    public AddressEntity updateAddress(Integer id, String zipcode, String street, String city, String state) {
        AddressEntity address = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (zipcode != null) address.setZipcode(zipcode);
        if (street != null) address.setStreet(street);
        if (city != null) address.setCity(city);
        if (state != null) address.setState(state);
        return addressRepository.save(address);
    }

    public void deleteAddress(Integer id) {
        addressRepository.deleteById(id);
    }

    // Requisição de empréstimo com análise de risco
    public ProposalEntity requestLoan(Integer customerId, BigDecimal amount, int installments,
                                   BigDecimal monthlyRate, LocalDate requestDate) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        ProposalEntity loan = new ProposalEntity()
            .setCustomerId(customerId)
            .setAmount(amount)
            .setNumberOfInstallments(installments)
            .setMonthlyInterestRate(monthlyRate)
            .setRequestDate(requestDate);
        
        RiskAssessmentEntity risk = riskAnalysisService.assessRisk(customer, loan);
        
        loan.setRiskAssessment(risk);

        // Gera as parcelas
        BigDecimal monthlyPayment = calculateMonthlyInstallment(amount, monthlyRate, installments);

        for (int i = 1; i <= installments; i++) {
            LocalDate dueDate = requestDate.plusMonths(i);
            InstallmentEntity installment = new InstallmentEntity()
                .setInstallmentNumber(i)
                .setDueDate(dueDate)
                .setAmount(monthlyPayment);

            installment.setLoanRequestId(loan.getId()); // id ainda nulo, será setado após save
            loan.getInstallments().add(installment);
        }

        ProposalEntity savedLoan = loanRequestRepository.save(loan);
        // Atualiza o loanRequestId nas parcelas e salva
        for (InstallmentEntity inst : savedLoan.getInstallments()) {
            inst.setLoanRequestId(savedLoan.getId());
            installmentRepository.save(inst);
        }
        
        customer.getLoanRequests().add(savedLoan);
        customerRepository.save(customer);
        
        return savedLoan;
    }

    private BigDecimal calculateMonthlyInstallment(BigDecimal amount, BigDecimal rate, int n) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        }
        BigDecimal factor = rate.multiply(BigDecimal.ONE.add(rate).pow(n))
                .divide(BigDecimal.ONE.add(rate).pow(n).subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);
        return amount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    // Pagamento de prestação
    public void payInstallment(Integer installmentId, LocalDate paymentDate) {
        InstallmentEntity installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("Installment not found"));
        if (installment.getPaidDate() != null) {
            throw new IllegalStateException("Installment already paid");
        }
        installment.setPaidDate(paymentDate);
        if (paymentDate.isAfter(installment.getDueDate())) {
            installment.setIsLate(true);
        }
        installmentRepository.save(installment);
    }

    // Liquidação antecipada – calcula o valor presente das parcelas restantes
    public BigDecimal earlySettlementValue(Integer loanRequestId, LocalDate settlementDate) {
        ProposalEntity loan = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        List<InstallmentEntity> installments = installmentRepository.findByLoanRequestId(loanRequestId);
        BigDecimal presentValue = BigDecimal.ZERO;
        BigDecimal monthlyRate = loan.getMonthlyInterestRate();

        for (InstallmentEntity inst : installments) {
            if (inst.getPaidDate() != null) continue; // já paga
            if (inst.getDueDate().isBefore(settlementDate)) continue; // vencida e não paga? Neste exemplo, ignoramos
            long monthsDiff = ChronoUnit.MONTHS.between(settlementDate.withDayOfMonth(1),
                    inst.getDueDate().withDayOfMonth(1));
            if (monthsDiff < 0) monthsDiff = 0;
            BigDecimal discountFactor = BigDecimal.ONE.add(monthlyRate).pow((int) monthsDiff);
            BigDecimal pv = inst.getAmount().divide(discountFactor, 2, RoundingMode.HALF_UP);
            presentValue = presentValue.add(pv);
        }
        return presentValue;
    }
}