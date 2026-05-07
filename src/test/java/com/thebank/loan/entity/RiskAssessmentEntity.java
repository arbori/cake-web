package com.thebank.loan.entity;

import java.util.Objects;

public class RiskAssessmentEntity {
    private Double probabilityOfPayment; // entre 0 e 1
    private Double debtToIncomeRatio;
    private Double zipcodeRiskFactor;
    private Double finalRiskScore; // 0 = baixo risco, 100 = alto risco

    public Double getProbabilityOfPayment() {
        return probabilityOfPayment;
    }
    public RiskAssessmentEntity setProbabilityOfPayment(Double probabilityOfPayment) {
        this.probabilityOfPayment = probabilityOfPayment;
        return this;
    }
    public Double getDebtToIncomeRatio() {
        return debtToIncomeRatio;
    }
    public RiskAssessmentEntity setDebtToIncomeRatio(Double debtToIncomeRatio) {
        this.debtToIncomeRatio = debtToIncomeRatio;
        return this;
    }
    public Double getZipcodeRiskFactor() {
        return zipcodeRiskFactor;
    }
    public RiskAssessmentEntity setZipcodeRiskFactor(Double zipcodeRiskFactor) {
        this.zipcodeRiskFactor = zipcodeRiskFactor;
        return this;
    }
    public Double getFinalRiskScore() {
        return finalRiskScore;
    }
    public RiskAssessmentEntity setFinalRiskScore(Double finalRiskScore) {
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
