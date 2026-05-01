package com.thebank.loan.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

public class RiskAnalysisService {
    // Mapeamento de faixas de CEP para fator de risco (exemplo)
    private static final Map<String, BigDecimal> ZIPCODE_RISK_FACTORS = new HashMap<>();
    
    static {
        ZIPCODE_RISK_FACTORS.put("01000", new BigDecimal("0.8"));  // baixo risco
        ZIPCODE_RISK_FACTORS.put("02000", new BigDecimal("1.0"));  // médio
        ZIPCODE_RISK_FACTORS.put("03000", new BigDecimal("1.2"));  // alto
        // fator padrão
    }

    private final AddressRepository addressRepository;
    private final InstallmentRepository installmentRepository;

    public RiskAnalysisService(AddressRepository addressRepository,
                               InstallmentRepository installmentRepository) {
        this.addressRepository = addressRepository;
        this.installmentRepository = installmentRepository;
    }

    public RiskAssessmentEntity assessRisk(CustomerEntity customer, ProposalEntity newLoan) {
        // 1. Probabilidade de pagamento baseada no histórico
        BigDecimal probability = calculatePaymentProbability(customer);

        // 2. Percentual de endividamento (novo compromisso + existentes)
        BigDecimal debtRatio = calculateDebtToIncomeRatio(customer, newLoan);

        // 3. Fator de risco do CEP
        BigDecimal zipcodeFactor = getZipcodeRiskFactor(customer.getAddressId());

        // 4. Cálculo do score final: quanto maior a probabilidade, menor o risco.
        //    Invertemos e aplicamos os fatores.
        //    Exemplo: riscoBase = (1 - probabilidade) * (1 + debtRatio) * zipcodeFactor
        BigDecimal riskBase = BigDecimal.ONE.subtract(probability)
                .multiply(BigDecimal.ONE.add(debtRatio))
                .multiply(zipcodeFactor);
        // Normaliza para escala 0-100
        BigDecimal finalScore = riskBase.multiply(new BigDecimal("100"))
                .min(BigDecimal.valueOf(100))
                .max(BigDecimal.ZERO);

        return new RiskAssessmentEntity()
            .setProbabilityOfPayment(probability)
            .setDebtToIncomeRatio(debtRatio)
            .setZipcodeRiskFactor(zipcodeFactor)
            .setFinalRiskScore(finalScore);
    }

    private BigDecimal calculatePaymentProbability(CustomerEntity customer) {
        List<ProposalEntity> loans = customer.getLoanRequests();
        if (loans.isEmpty()) {
            return BigDecimal.ONE; // sem histórico = risco máximo? Aqui consideramos neutro
        }
        List<InstallmentEntity> allInstallments = installmentRepository.findByCustomerId(customer.getId(), loans);
        if (allInstallments.isEmpty()) {
            return BigDecimal.ONE;
        }
        long total = allInstallments.size();
        long problematic = allInstallments.stream()
                .filter(i -> i.getPaidDate() == null || Boolean.TRUE.equals(i.getIsLate()))
                .count();
        if (total == 0) return BigDecimal.ONE;
        return BigDecimal.valueOf(total - problematic).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDebtToIncomeRatio(CustomerEntity customer, ProposalEntity newLoan) {
        BigDecimal monthlyInstallmentNewLoan = calculateMonthlyInstallment(newLoan);
        BigDecimal existingMonthlyCommitment = customer.getLoanRequests().stream()
                .map(this::calculateMonthlyInstallment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMonthly = existingMonthlyCommitment.add(monthlyInstallmentNewLoan);
        if (customer.getSalary().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(100);
        return totalMonthly.divide(customer.getSalary(), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlyInstallment(ProposalEntity loan) {
        // fórmula de prestação fixa (Price)
        BigDecimal rate = loan.getMonthlyInterestRate();
        BigDecimal principal = loan.getAmount();
        int n = loan.getNumberOfInstallments();
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        }
        BigDecimal factor = rate.multiply(BigDecimal.ONE.add(rate).pow(n))
                .divide(BigDecimal.ONE.add(rate).pow(n).subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);
        return principal.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getZipcodeRiskFactor(Integer addressId) {
        return addressRepository.findById(addressId)
                .map(AddressEntity::getZipcode)
                .map(zip -> {
                    String prefix = zip.length() >= 5 ? zip.substring(0, 5) : zip;
                    return ZIPCODE_RISK_FACTORS.getOrDefault(prefix, new BigDecimal("1.0"));
                })
                .orElse(new BigDecimal("1.0"));
    }
}
