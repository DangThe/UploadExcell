package com.vrbank.uploadexcel.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Excel upload parameters
 * Contains all parameters needed to process an Excel file upload
 */
public class ExcelUploadDTO {

    @NotNull(message = "Batch number is required")
    @Size(max = 20, message = "Batch number cannot exceed 20 characters")
    private String batchNo;

    @NotNull(message = "Branch code is required")
    @Size(max = 10, message = "Branch code cannot exceed 10 characters")
    private String branchCode;

    @NotNull(message = "Source code is required")
    @Size(max = 10, message = "Source code cannot exceed 10 characters")
    private String sourceCode;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Exchange rate must be greater than zero")
    private BigDecimal exchRate;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    // Constructors
    public ExcelUploadDTO() {}

    public ExcelUploadDTO(String batchNo, String branchCode, String sourceCode, BigDecimal exchRate, LocalDate entryDate) {
        this.batchNo = batchNo;
        this.branchCode = branchCode;
        this.sourceCode = sourceCode;
        this.exchRate = exchRate;
        this.entryDate = entryDate;
    }

    // Getters and Setters
    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public BigDecimal getExchRate() {
        return exchRate;
    }

    public void setExchRate(BigDecimal exchRate) {
        this.exchRate = exchRate;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    // Validation methods
    public boolean isValid() {
        return (
            batchNo != null &&
            !batchNo.trim().isEmpty() &&
            branchCode != null &&
            !branchCode.trim().isEmpty() &&
            sourceCode != null &&
            !sourceCode.trim().isEmpty() &&
            exchRate != null &&
            exchRate.compareTo(BigDecimal.ZERO) > 0 &&
            entryDate != null
        );
    }

    @Override
    public String toString() {
        return (
            "ExcelUploadDTO{" +
            "batchNo='" +
            batchNo +
            '\'' +
            ", branchCode='" +
            branchCode +
            '\'' +
            ", sourceCode='" +
            sourceCode +
            '\'' +
            ", exchRate=" +
            exchRate +
            ", entryDate=" +
            entryDate +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExcelUploadDTO that = (ExcelUploadDTO) o;

        if (!batchNo.equals(that.batchNo)) return false;
        if (!branchCode.equals(that.branchCode)) return false;
        if (!sourceCode.equals(that.sourceCode)) return false;
        if (!exchRate.equals(that.exchRate)) return false;
        return entryDate.equals(that.entryDate);
    }

    @Override
    public int hashCode() {
        int result = batchNo.hashCode();
        result = 31 * result + branchCode.hashCode();
        result = 31 * result + sourceCode.hashCode();
        result = 31 * result + exchRate.hashCode();
        result = 31 * result + entryDate.hashCode();
        return result;
    }
}
