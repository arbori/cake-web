package com.thebank.loan.service;

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
import com.thebank.loan.repository.memory.InMemoryAddressRepository;
import com.thebank.loan.repository.memory.InMemoryCustomerRepository;
import com.thebank.loan.repository.memory.InMemoryInstallmentRepository;
import com.thebank.loan.repository.memory.InMemoryProposalRepository;

import loan.capture.AddressResponse;
import loan.capture.CustomerResponse;
import loan.capture.ProposalResponse;

public class LoanService {
    private final CustomerRepository customerRepository = InMemoryCustomerRepository.instance();
    private final AddressRepository addressRepository = InMemoryAddressRepository.instance();
    private final ProposalRepository loanRequestRepository = InMemoryProposalRepository.instance();
    private final InstallmentRepository installmentRepository = InMemoryInstallmentRepository.instance();

    private final RiskAnalysisService riskAnalysisService = new RiskAnalysisService();

    // CRUD Customer
    public CustomerResponse createCustomer(String name, Double salary, Integer addressId) {
        CustomerEntity savedCustomer = customerRepository.save(new CustomerEntity()
            .setName(name)
            .setSalary(salary)
            .setAddressId(addressId));

        return new CustomerResponse()
                .setId(savedCustomer.getId())
                .setName(savedCustomer.getName())
                .setSalary(savedCustomer.getSalary())
                .setAddressId(savedCustomer.getAddressId());
    }

    public Optional<CustomerResponse> getCustomer(Integer id) {
        CustomerResponse response = new CustomerResponse();

        customerRepository.findById(id).ifPresent(customer -> 
            response.setId(customer.getId())
                .setName(customer.getName())
                .setSalary(customer.getSalary())
                .setAddressId(customer.getAddressId()));

        return Optional.of(response);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customer -> new CustomerResponse()
                        .setId(customer.getId())
                        .setName(customer.getName())
                        .setSalary(customer.getSalary())
                        .setAddressId(customer.getAddressId()))
                .toList();
    }

    public CustomerResponse updateCustomer(Integer id, String name, Double salary, Integer addressId) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        if (name != null) customer.setName(name);
        if (salary != null) customer.setSalary(salary);
        if (addressId != null) {
            if (!addressRepository.existsById(addressId)) throw new IllegalArgumentException("Address not found");
        
            customer.setAddressId(addressId);
        }
        
        customer = customerRepository.save(customer);

        return new CustomerResponse()
                .setId(customer.getId())
                .setName(customer.getName())
                .setSalary(customer.getSalary())
                .setAddressId(customer.getAddressId());
    }

    public CustomerResponse deleteCustomer(Integer id) {
        CustomerResponse response =new CustomerResponse();

        customerRepository.findById(id).ifPresent(customer -> 
            response
                .setId(customer.getId())
                .setName(customer.getName())
                .setSalary(customer.getSalary())
                .setAddressId(customer.getAddressId())
        );

        customerRepository.deleteById(id);

        return response;
    }

    // CRUD Address
    public AddressResponse createAddress(String zipcode, String street, String city, String state) {
        AddressEntity address = new AddressEntity()
            .setZipcode(zipcode)
            .setStreet(street)
            .setCity(city)
            .setState(state);

        address = addressRepository.save(address);

        return new AddressResponse()
                .setId(address.getId())
                .setZipcode(address.getZipcode())
                .setStreet(address.getStreet())
                .setCity(address.getCity())
                .setState(address.getState());
    }

    public Optional<AddressResponse> getAddress(Integer id) {
        return addressRepository.findById(id).map(address -> new AddressResponse()
                .setId(address.getId())
                .setZipcode(address.getZipcode())
                .setStreet(address.getStreet())
                .setCity(address.getCity())
                .setState(address.getState()));
    }

    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAll().stream()
                .map(address -> new AddressResponse()
                        .setId(address.getId())
                        .setZipcode(address.getZipcode())
                        .setStreet(address.getStreet())
                        .setCity(address.getCity())
                        .setState(address.getState()))
                .toList();
    }

    public AddressResponse updateAddress(Integer id, String zipcode, String street, String city, String state) {
        AddressEntity address = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (zipcode != null) address.setZipcode(zipcode);
        if (street != null) address.setStreet(street);
        if (city != null) address.setCity(city);
        if (state != null) address.setState(state);

        address = addressRepository.save(address);
        
        return new AddressResponse()
                .setId(address.getId())
                .setZipcode(address.getZipcode())
                .setStreet(address.getStreet())
                .setCity(address.getCity())
                .setState(address.getState());
    }

    public void deleteAddress(Integer id) {
        addressRepository.deleteById(id);
    }

    // Requisição de empréstimo com análise de risco
    public ProposalResponse requestLoan(Integer customerId, Double amount, int installments,
                                   Double monthlyRate, LocalDate requestDate) {
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
        Double monthlyPayment = calculateMonthlyInstallment(amount, monthlyRate, installments);

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
        
        return new ProposalResponse()
                .setId(savedLoan.getId())
                .setCustomerId(savedLoan.getCustomerId())
                .setAmount(savedLoan.getAmount())
                .setNumberOfInstallments(savedLoan.getNumberOfInstallments())
                .setMonthlyInterestRate(savedLoan.getMonthlyInterestRate())
                .setRequestDate(savedLoan.getRequestDate());
    }

    private Double calculateMonthlyInstallment(Double amount, Double rate, int n) {
        if (rate == 0.0) {
            return amount / (double) n;
        }
        double factor = rate * Math.pow(1 + rate, n) / (Math.pow(1 + rate, n) - 1);
        return amount * factor;
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
    public double earlySettlementValue(Integer loanRequestId, LocalDate settlementDate) {
        ProposalEntity loan = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        List<InstallmentEntity> installments = installmentRepository.findByLoanRequestId(loanRequestId);
        double presentValue = 0.0;
        Double monthlyRate = loan.getMonthlyInterestRate();

        for (InstallmentEntity inst : installments) {
            if (inst.getPaidDate() != null) continue; // já paga
            if (inst.getDueDate().isBefore(settlementDate)) continue; // vencida e não paga? Neste exemplo, ignoramos
            long monthsDiff = ChronoUnit.MONTHS.between(settlementDate.withDayOfMonth(1),
                    inst.getDueDate().withDayOfMonth(1));
            if (monthsDiff < 0) monthsDiff = 0;
            double discountFactor = Math.pow(1 + monthlyRate, (int) monthsDiff);
            double pv = inst.getAmount() / discountFactor;
            presentValue = presentValue + pv;
        }
        return presentValue;
    }

    public Optional<ProposalResponse> getProposal(Integer customerId,Integer proposalId) {
        return loanRequestRepository.findById(proposalId).map(loan -> new ProposalResponse()
                .setId(loan.getId())
                .setCustomerId(loan.getCustomerId())
                .setAmount(loan.getAmount())
                .setNumberOfInstallments(loan.getNumberOfInstallments())
                .setMonthlyInterestRate(loan.getMonthlyInterestRate())
                .setRequestDate(loan.getRequestDate()));
    }

    public List<CustomerResponse> getCustomersByCity(String cityName) {
        List<Integer> addressIds = addressRepository.findAll().stream()
            .filter(addr -> addr.getCity().equalsIgnoreCase(cityName))
            .map(AddressEntity::getId)
            .toList();

        return customerRepository.findAll().stream()
            .filter(customer -> addressIds.contains(customer.getAddressId()))
            .map(customer -> new CustomerResponse()
                    .setId(customer.getId())
                    .setName(customer.getName())
                    .setSalary(customer.getSalary())
                    .setAddressId(customer.getAddressId()))
            .toList();
    }
}