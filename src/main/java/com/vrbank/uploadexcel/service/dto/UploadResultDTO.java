package com.vrbank.uploadexcel.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for upload result
 * Contains the complete result of an Excel file upload operation
 */
public class UploadResultDTO {

    private boolean success;
    private String message;
    private String batchNo;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<UploadErrorDTO> errors;
    private long processingTimeMs;
    private String uploadTimestamp;

    // Constructors
    public UploadResultDTO() {
        this.errors = new ArrayList<>();
        this.processingTimeMs = 0;
    }

    public UploadResultDTO(boolean success, String message, String batchNo) {
        this();
        this.success = success;
        this.message = message;
        this.batchNo = batchNo;
    }

    // Factory methods for common scenarios
    public static UploadResultDTO success(String batchNo, int totalRows, int successCount) {
        UploadResultDTO result = new UploadResultDTO();
        result.setSuccess(true);
        result.setBatchNo(batchNo);
        result.setTotalRows(totalRows);
        result.setSuccessCount(successCount);
        result.setErrorCount(0);
        result.setMessage(String.format("Upload completed successfully. %d rows processed", successCount));
        return result;
    }

    public static UploadResultDTO error(String batchNo, String message) {
        UploadResultDTO result = new UploadResultDTO();
        result.setSuccess(false);
        result.setBatchNo(batchNo);
        result.setMessage(message);
        result.setTotalRows(0);
        result.setSuccessCount(0);
        result.setErrorCount(0);
        return result;
    }

    public static UploadResultDTO partialSuccess(String batchNo, int totalRows, int successCount, int errorCount) {
        UploadResultDTO result = new UploadResultDTO();
        result.setSuccess(errorCount == 0);
        result.setBatchNo(batchNo);
        result.setTotalRows(totalRows);
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);

        if (errorCount > 0) {
            result.setMessage(String.format("Upload completed with errors. %d/%d rows processed successfully", successCount, totalRows));
        } else {
            result.setMessage(String.format("Upload completed successfully. %d rows processed", successCount));
        }
        return result;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<UploadErrorDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<UploadErrorDTO> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(String uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    // Helper methods
    public void addError(UploadErrorDTO error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.errorCount = this.errors.size();
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public double getSuccessRate() {
        if (totalRows == 0) return 0.0;
        return ((double) successCount / totalRows) * 100.0;
    }

    public double getErrorRate() {
        if (totalRows == 0) return 0.0;
        return ((double) errorCount / totalRows) * 100.0;
    }

    public String getProcessingTimeSummary() {
        if (processingTimeMs < 1000) {
            return processingTimeMs + " ms";
        } else {
            return String.format("%.2f seconds", processingTimeMs / 1000.0);
        }
    }

    @Override
    public String toString() {
        return (
            "UploadResultDTO{" +
            "success=" +
            success +
            ", message='" +
            message +
            '\'' +
            ", batchNo='" +
            batchNo +
            '\'' +
            ", totalRows=" +
            totalRows +
            ", successCount=" +
            successCount +
            ", errorCount=" +
            errorCount +
            ", errorsList=" +
            (errors != null ? errors.size() : 0) +
            ", processingTimeMs=" +
            processingTimeMs +
            ", successRate=" +
            String.format("%.1f%%", getSuccessRate()) +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UploadResultDTO that = (UploadResultDTO) o;

        if (success != that.success) return false;
        if (totalRows != that.totalRows) return false;
        if (successCount != that.successCount) return false;
        if (errorCount != that.errorCount) return false;
        if (!batchNo.equals(that.batchNo)) return false;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + message.hashCode();
        result = 31 * result + batchNo.hashCode();
        result = 31 * result + totalRows;
        result = 31 * result + successCount;
        result = 31 * result + errorCount;
        return result;
    }
}
