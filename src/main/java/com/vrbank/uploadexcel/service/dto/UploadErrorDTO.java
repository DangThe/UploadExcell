package com.vrbank.uploadexcel.service.dto;

import java.math.BigDecimal;

/**
 * DTO for upload errors
 * Contains detailed information about errors that occurred during Excel upload
 */
public class UploadErrorDTO {

    private int rowNumber;
    private String errorMessage;
    private String errorCode;
    private String relCust;
    private String account;
    private String accountBranch;
    private String drCr;
    private String ccyCd;
    private BigDecimal amount;
    private BigDecimal lcyEquivalent;
    private String txnCode;
    private String addlText;
    private String severity; // ERROR, WARNING, INFO

    // Constructors
    public UploadErrorDTO() {
        this.severity = "ERROR";
    }

    public UploadErrorDTO(int rowNumber, String errorMessage) {
        this();
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
    }

    public UploadErrorDTO(int rowNumber, String errorMessage, String errorCode) {
        this(rowNumber, errorMessage);
        this.errorCode = errorCode;
    }

    // Factory methods for common error types
    public static UploadErrorDTO validationError(int rowNumber, String field, String message) {
        UploadErrorDTO error = new UploadErrorDTO();
        error.setRowNumber(rowNumber);
        error.setErrorCode("VALIDATION_ERROR");
        error.setErrorMessage(String.format("Field '%s': %s", field, message));
        error.setSeverity("ERROR");
        return error;
    }

    public static UploadErrorDTO accountError(int rowNumber, String account, String message) {
        UploadErrorDTO error = new UploadErrorDTO();
        error.setRowNumber(rowNumber);
        error.setErrorCode("ACCOUNT_ERROR");
        error.setErrorMessage(message);
        error.setAccount(account);
        error.setSeverity("ERROR");
        return error;
    }

    public static UploadErrorDTO amountError(int rowNumber, String message, BigDecimal amount, String currency) {
        UploadErrorDTO error = new UploadErrorDTO();
        error.setRowNumber(rowNumber);
        error.setErrorCode("AMOUNT_ERROR");
        error.setErrorMessage(message);
        error.setAmount(amount);
        error.setCcyCd(currency);
        error.setSeverity("ERROR");
        return error;
    }

    public static UploadErrorDTO processingError(int rowNumber, String message) {
        UploadErrorDTO error = new UploadErrorDTO();
        error.setRowNumber(rowNumber);
        error.setErrorCode("PROCESSING_ERROR");
        error.setErrorMessage(message);
        error.setSeverity("ERROR");
        return error;
    }

    public static UploadErrorDTO warning(int rowNumber, String message) {
        UploadErrorDTO error = new UploadErrorDTO();
        error.setRowNumber(rowNumber);
        error.setErrorCode("WARNING");
        error.setErrorMessage(message);
        error.setSeverity("WARNING");
        return error;
    }

    // Getters and Setters
    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getRelCust() {
        return relCust;
    }

    public void setRelCust(String relCust) {
        this.relCust = relCust;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountBranch() {
        return accountBranch;
    }

    public void setAccountBranch(String accountBranch) {
        this.accountBranch = accountBranch;
    }

    public String getDrCr() {
        return drCr;
    }

    public void setDrCr(String drCr) {
        this.drCr = drCr;
    }

    public String getCcyCd() {
        return ccyCd;
    }

    public void setCcyCd(String ccyCd) {
        this.ccyCd = ccyCd;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getLcyEquivalent() {
        return lcyEquivalent;
    }

    public void setLcyEquivalent(BigDecimal lcyEquivalent) {
        this.lcyEquivalent = lcyEquivalent;
    }

    public String getTxnCode() {
        return txnCode;
    }

    public void setTxnCode(String txnCode) {
        this.txnCode = txnCode;
    }

    public String getAddlText() {
        return addlText;
    }

    public void setAddlText(String addlText) {
        this.addlText = addlText;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    // Helper methods
    public boolean isError() {
        return "ERROR".equals(severity);
    }

    public boolean isWarning() {
        return "WARNING".equals(severity);
    }

    public boolean isInfo() {
        return "INFO".equals(severity);
    }

    public String getDisplayMessage() {
        return String.format("Row %d: %s", rowNumber, errorMessage);
    }

    public String getCSVLine() {
        return String.format(
            "%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
            rowNumber,
            errorMessage != null ? errorMessage.replace("\"", "\"\"") : "",
            relCust != null ? relCust : "",
            account != null ? account : "",
            accountBranch != null ? accountBranch : "",
            drCr != null ? drCr : "",
            ccyCd != null ? ccyCd : "",
            amount != null ? amount.toString() : "",
            lcyEquivalent != null ? lcyEquivalent.toString() : "",
            txnCode != null ? txnCode : "",
            addlText != null ? addlText.replace("\"", "\"\"") : ""
        );
    }

    @Override
    public String toString() {
        return (
            "UploadErrorDTO{" +
            "rowNumber=" +
            rowNumber +
            ", errorMessage='" +
            errorMessage +
            '\'' +
            ", errorCode='" +
            errorCode +
            '\'' +
            ", account='" +
            account +
            '\'' +
            ", amount=" +
            amount +
            ", ccyCd='" +
            ccyCd +
            '\'' +
            ", severity='" +
            severity +
            '\'' +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UploadErrorDTO that = (UploadErrorDTO) o;

        if (rowNumber != that.rowNumber) return false;
        if (!errorMessage.equals(that.errorMessage)) return false;
        return errorCode != null ? errorCode.equals(that.errorCode) : that.errorCode == null;
    }

    @Override
    public int hashCode() {
        int result = rowNumber;
        result = 31 * result + errorMessage.hashCode();
        result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
        return result;
    }
}
