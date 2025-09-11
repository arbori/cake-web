package com.bank.loan.business.dto;

public class RiskDTO {
    private String riskId;
    private String level;
    private String description;

    public RiskDTO() {
    }

    public RiskDTO(String riskId, String level, String description) {
        this.riskId = riskId;
        this.level = level;
        this.description = description;
    }

    public String getRiskId() {
        return riskId;
    }

    public RiskDTO setRiskId(String riskId) {
        this.riskId = riskId;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public RiskDTO setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RiskDTO setDescription(String description) {
        this.description = description;
        return this;
    }
}
