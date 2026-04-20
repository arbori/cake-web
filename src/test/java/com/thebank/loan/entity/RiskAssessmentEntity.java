package com.thebank.loan.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class RiskAssessmentEntity {
    private BigDecimal probabilityOfPayment; // entre 0 e 1
    private BigDecimal debtToIncomeRatio;
    private BigDecimal zipcodeRiskFactor;
    private BigDecimal finalRiskScore; // 0 = baixo risco, 100 = alto risco

    public BigDecimal getProbabilityOfPayment() {
        return probabilityOfPayment;
    }
    public RiskAssessmentEntity setProbabilityOfPayment(BigDecimal probabilityOfPayment) {
        this.probabilityOfPayment = probabilityOfPayment;
        return this;
    }
    public BigDecimal getDebtToIncomeRatio() {
        return debtToIncomeRatio;
    }
    public RiskAssessmentEntity setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) {
        this.debtToIncomeRatio = debtToIncomeRatio;
        return this;
    }
    public BigDecimal getZipcodeRiskFactor() {
        return zipcodeRiskFactor;
    }
    public RiskAssessmentEntity setZipcodeRiskFactor(BigDecimal zipcodeRiskFactor) {
        this.zipcodeRiskFactor = zipcodeRiskFactor;
        return this;
    }
    public BigDecimal getFinalRiskScore() {
        return finalRiskScore;
    }
    public RiskAssessmentEntity setFinalRiskScore(BigDecimal finalRiskScore) {
        this.finalRiskScore = finalRiskScore;
        return this;
    }

    @Override
    public String toString() {
        return "'RiskAssessmentData': {'probabilityOfPayment'=" + probabilityOfPayment + ", 'debtToIncomeRatio'=" + debtToIncomeRatio + ", 'zipcodeRiskFactor'=" + zipcodeRiskFactor + ", 'finalRiskScore'=" + finalRiskScore + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(probabilityOfPayment, debtToIncomeRatio, zipcodeRiskFactor, finalRiskScore);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
