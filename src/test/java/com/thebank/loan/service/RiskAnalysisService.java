package com.thebank.loan.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thebank.loan.entity.AddressEntity;
import com.thebank.loan.entity.CustomerEntity;
import com.thebank.loan.entity.InstallmentEntity;
import com.thebank.loan.entity.ProposalEntity;
import com.thebank.loan.entity.RiskAssessmentEntity;
import com.thebank.loan.repository.AddressRepository;
import com.thebank.loan.repository.InstallmentRepository;
import com.thebank.loan.repository.memory.InMemoryAddressRepository;
import com.thebank.loan.repository.memory.InMemoryInstallmentRepository;

public class RiskAnalysisService {
    // Mapeamento de faixas de CEP para fator de risco (exemplo)
    private static final Map<String, Double> ZIPCODE_RISK_FACTORS = new HashMap<>();
    
    static {
        ZIPCODE_RISK_FACTORS.put("01000", 0.8);  // baixo risco
        ZIPCODE_RISK_FACTORS.put("02000", 1.0);  // médio
        ZIPCODE_RISK_FACTORS.put("03000", 1.2);  // alto
        // fator padrão
    }

    private final AddressRepository addressRepository = InMemoryAddressRepository.instance();
    private final InstallmentRepository installmentRepository = InMemoryInstallmentRepository.instance();

    public RiskAssessmentEntity assessRisk(CustomerEntity customer, ProposalEntity newLoan) {
        // 1. Probabilidade de pagamento baseada no histórico
        double probability = calculatePaymentProbability(customer);

        // 2. Percentual de endividamento (novo compromisso + existentes)
        double debtRatio = calculateDebtToIncomeRatio(customer, newLoan);

        // 3. Fator de risco do CEP
        double zipcodeFactor = getZipcodeRiskFactor(customer.getAddressId());

        // 4. Cálculo do score final: quanto maior a probabilidade, menor o risco.
        //    Invertemos e aplicamos os fatores.
        //    Exemplo: riscoBase = (1 - probabilidade) * (1 + debtRatio) * zipcodeFactor
        double riskBase = 1 - probability;
        riskBase = riskBase * (1 + debtRatio) * zipcodeFactor;
        // Normaliza para escala 0-100
        double finalScore = riskBase * 100;
        finalScore = Math.min(finalScore, 100);
        finalScore = Math.max(finalScore, 0);

        return new RiskAssessmentEntity()
            .setProbabilityOfPayment(probability)
            .setDebtToIncomeRatio(debtRatio)
            .setZipcodeRiskFactor(zipcodeFactor)
            .setFinalRiskScore(finalScore);
    }

    private double calculatePaymentProbability(CustomerEntity customer) {
        List<ProposalEntity> loans = customer.getLoanRequests();
        if (loans.isEmpty()) {
            return 1.0; // sem histórico = risco máximo? Aqui consideramos neutro
        }
        List<InstallmentEntity> allInstallments = installmentRepository.findByCustomerId(customer.getId(), loans);
        if (allInstallments.isEmpty()) {
            return 1.0;
        }
        long total = allInstallments.size();
        long problematic = allInstallments.stream()
                .filter(i -> i.getPaidDate() == null || Boolean.TRUE.equals(i.getIsLate()))
                .count();
        if (total == 0) return 1.0;
        return (total - problematic) / total;
    }

    private double calculateDebtToIncomeRatio(CustomerEntity customer, ProposalEntity newLoan) {
        double monthlyInstallmentNewLoan = calculateMonthlyInstallment(newLoan);
        double existingMonthlyCommitment = customer.getLoanRequests().stream()
                .map(this::calculateMonthlyInstallment)
                .reduce(0.0, Double::sum);
        double totalMonthly = existingMonthlyCommitment + monthlyInstallmentNewLoan;
        if (customer.getSalary() == 0) return 100.0;
        return totalMonthly / customer.getSalary();
    }

    private double calculateMonthlyInstallment(ProposalEntity loan) {
        // fórmula de prestação fixa (Price)
        double rate = loan.getMonthlyInterestRate();
        double principal = loan.getAmount();
        int n = loan.getNumberOfInstallments();
        if (rate == 0) {
            return principal / n;
        }
        double factor = rate * Math.pow(1 + rate, n) / (Math.pow(1 + rate, n) - 1);
        return principal * factor;
    }

    private double getZipcodeRiskFactor(Integer addressId) {
        return addressRepository.findById(addressId)
                .map(AddressEntity::getZipcode)
                .map(zip -> {
                    String prefix = zip.length() >= 5 ? zip.substring(0, 5) : zip;
                    return ZIPCODE_RISK_FACTORS.getOrDefault(prefix, 1.0);
                })
                .orElse(1.0);
    }
}
