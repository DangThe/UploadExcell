package com.vrbank.uploadexcel.service;

import com.vrbank.uploadexcel.domain.UploadDetail;
import com.vrbank.uploadexcel.repository.UploadDetailRepository;
import com.vrbank.uploadexcel.service.dto.ExcelUploadDTO;
import com.vrbank.uploadexcel.service.dto.UploadErrorDTO;
import com.vrbank.uploadexcel.service.dto.UploadResultDTO;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for processing Excel file uploads
 * Handles file parsing, validation, and data persistence
 */
@Service
@Transactional
public class ExcelUploadService {

    private final Logger log = LoggerFactory.getLogger(ExcelUploadService.class);

    private final UploadDetailRepository uploadDetailRepository;
    private final AccountValidationService accountValidationService;

    // Excel column indices (0-based)
    private static final int COL_REL_CUST = 1; // Column B
    private static final int COL_ACCOUNT = 2; // Column C
    private static final int COL_ACCOUNT_BRANCH = 3; // Column D
    private static final int COL_DR_CR = 4; // Column E
    private static final int COL_CCY_CD = 5; // Column F
    private static final int COL_AMOUNT = 6; // Column G
    private static final int COL_LCY_EQUIVALENT = 7; // Column H
    private static final int COL_TXN_CODE = 8; // Column I
    private static final int COL_ADDL_TEXT = 9; // Column J

    private static final int START_ROW = 2; // Skip header rows (0-based, so row 3)
    private static final int MAX_ROWS_PER_BATCH = 10000; // Configurable limit

    public ExcelUploadService(UploadDetailRepository uploadDetailRepository, AccountValidationService accountValidationService) {
        this.uploadDetailRepository = uploadDetailRepository;
        this.accountValidationService = accountValidationService;
    }

    /**
     * Process Excel file upload
     * Main method that orchestrates the entire upload process
     */
    public UploadResultDTO processExcelUpload(MultipartFile file, ExcelUploadDTO uploadParams) {
        log.info("Starting Excel file upload processing for batch: {}", uploadParams.getBatchNo());

        long startTime = System.currentTimeMillis();
        UploadResultDTO result = new UploadResultDTO();
        result.setBatchNo(uploadParams.getBatchNo());
        result.setErrors(new ArrayList<>());
        result.setUploadTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            // Validate upload parameters
            String paramValidation = validateUploadParameters(uploadParams);
            if (!AccountValidationService.RESULT_OK.equals(paramValidation)) {
                return UploadResultDTO.error(uploadParams.getBatchNo(), paramValidation);
            }

            // Check if batch already exists
            if (uploadDetailRepository.existsByBatchNo(uploadParams.getBatchNo())) {
                String message = "Batch " + uploadParams.getBatchNo() + " already exists in the system";
                log.warn(message);
                return UploadResultDTO.error(uploadParams.getBatchNo(), message);
            }

            // Validate and process file
            result = processExcelFile(file, uploadParams, startTime);
        } catch (Exception e) {
            log.error("Unexpected error during Excel upload processing for batch {}: {}", uploadParams.getBatchNo(), e.getMessage(), e);
            result = UploadResultDTO.error(uploadParams.getBatchNo(), "Unexpected error during processing: " + e.getMessage());
        }

        long processingTime = System.currentTimeMillis() - startTime;
        result.setProcessingTimeMs(processingTime);

        log.info(
            "Excel upload processing completed for batch {}. Success: {}, Time: {}ms",
            uploadParams.getBatchNo(),
            result.isSuccess(),
            processingTime
        );

        return result;
    }

    /**
     * Validate upload parameters
     */
    private String validateUploadParameters(ExcelUploadDTO uploadParams) {
        if (!uploadParams.isValid()) {
            return "Invalid upload parameters";
        }

        // Additional business validations can be added here
        return AccountValidationService.RESULT_OK;
    }

    /**
     * Process the Excel file
     */
    private UploadResultDTO processExcelFile(MultipartFile file, ExcelUploadDTO uploadParams, long startTime) throws IOException {
        log.debug("Processing Excel file: {} ({}KB)", file.getOriginalFilename(), file.getSize() / 1024);

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = createWorkbook(file, inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // Validate sheet structure
            String sheetValidation = validateSheetStructure(sheet);
            if (!AccountValidationService.RESULT_OK.equals(sheetValidation)) {
                return UploadResultDTO.error(uploadParams.getBatchNo(), sheetValidation);
            }

            List<UploadDetail> uploadDetails = new ArrayList<>();
            List<UploadErrorDTO> errors = new ArrayList<>();
            int successCount = 0;
            int currentRow = START_ROW;

            // Process each data row
            for (Row row : sheet) {
                if (row.getRowNum() < START_ROW) {
                    continue; // Skip header rows
                }

                currentRow = row.getRowNum() + 1; // 1-based for user display

                // Check row limit
                if (currentRow - START_ROW > MAX_ROWS_PER_BATCH) {
                    log.warn("Row limit exceeded for batch {}. Max rows: {}", uploadParams.getBatchNo(), MAX_ROWS_PER_BATCH);
                    break;
                }

                try {
                    UploadDetail uploadDetail = processRow(row, uploadParams, currentRow);
                    if (uploadDetail != null) {
                        // Validate the record
                        List<UploadErrorDTO> rowErrors = validateUploadDetail(uploadDetail, currentRow);
                        if (rowErrors.isEmpty()) {
                            uploadDetails.add(uploadDetail);
                            successCount++;
                        } else {
                            errors.addAll(rowErrors);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", currentRow, e.getMessage());
                    errors.add(UploadErrorDTO.processingError(currentRow, "Error processing row: " + e.getMessage()));
                }
            }

            workbook.close();

            // Save successful records if any
            if (!uploadDetails.isEmpty()) {
                uploadDetailRepository.saveAll(uploadDetails);
                log.info("Saved {} records for batch {}", uploadDetails.size(), uploadParams.getBatchNo());
            }

            // Build result
            UploadResultDTO result = UploadResultDTO.partialSuccess(
                uploadParams.getBatchNo(),
                successCount + errors.size(),
                successCount,
                errors.size()
            );
            result.setErrors(errors);
            result.setUploadTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return result;
        }
    }

    /**
     * Create appropriate workbook based on file type
     */
    private Workbook createWorkbook(MultipartFile file, InputStream inputStream) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (filename != null && filename.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IOException("Unsupported file format. Only .xlsx and .xls files are supported.");
        }
    }

    /**
     * Validate Excel sheet structure
     */
    private String validateSheetStructure(Sheet sheet) {
        if (sheet == null) {
            return "Excel file contains no sheets";
        }

        if (sheet.getLastRowNum() < START_ROW) {
            return "Excel file contains no data rows. Expected data starting from row " + (START_ROW + 1);
        }

        // Validate header row (optional but recommended)
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            // Basic header validation can be added here
            log.debug("Header row found with {} columns", headerRow.getLastCellNum());
        }

        return AccountValidationService.RESULT_OK;
    }

    /**
     * Process individual row from Excel
     */
    private UploadDetail processRow(Row row, ExcelUploadDTO uploadParams, int rowNumber) {
        // Check if row is empty
        if (isRowEmpty(row)) {
            log.debug("Skipping empty row {}", rowNumber);
            return null;
        }

        UploadDetail detail = new UploadDetail();

        try {
            // Set fixed parameters from upload form
            detail.setBatchNo(uploadParams.getBatchNo());
            detail.setBranchCode(uploadParams.getBranchCode());
            detail.setSourceCode(uploadParams.getSourceCode());
            detail.setExchRate(uploadParams.getExchRate());
            detail.setInitiationDate(uploadParams.getEntryDate());
            detail.setValueDate(uploadParams.getEntryDate());
            detail.setUploadDate(LocalDate.now());
            detail.setFinCycle("FY" + uploadParams.getEntryDate().getYear());
            detail.setPeriodCode(getMonthString(uploadParams.getEntryDate().getMonthValue()));
            detail.setCurrNo(String.valueOf(rowNumber - START_ROW));

            // Read data from Excel columns
            detail.setAccount(getCellStringValue(row.getCell(COL_ACCOUNT)));

            // Set REL_CUST based on account length (customer accounts are 15 digits)
            String account = detail.getAccount();
            if (account != null && account.length() >= 15) {
                detail.setRelCust(getCellStringValue(row.getCell(COL_REL_CUST)));
            }

            detail.setAccountBranch(getCellStringValue(row.getCell(COL_ACCOUNT_BRANCH)));
            detail.setDrCr(getCellStringValue(row.getCell(COL_DR_CR)));
            detail.setCcyCd(getCellStringValue(row.getCell(COL_CCY_CD)));
            detail.setAmount(getCellNumericValue(row.getCell(COL_AMOUNT)));
            detail.setLcyEquivalent(getCellNumericValue(row.getCell(COL_LCY_EQUIVALENT)));
            detail.setTxnCode(getCellStringValue(row.getCell(COL_TXN_CODE)));
            detail.setAddlText(getCellStringValue(row.getCell(COL_ADDL_TEXT)));

            // Set default values
            detail.setUploadStat("N");
            detail.setDeleteStat("N");

            return detail;
        } catch (Exception e) {
            log.error("Error parsing row {}: {}", rowNumber, e.getMessage());
            throw new RuntimeException("Error parsing Excel row data", e);
        }
    }

    /**
     * Validate upload detail record
     */
    private List<UploadErrorDTO> validateUploadDetail(UploadDetail detail, int rowNumber) {
        List<UploadErrorDTO> errors = new ArrayList<>();

        // Basic field validations
        if (detail.getAccount() == null || detail.getAccount().trim().isEmpty()) {
            errors.add(UploadErrorDTO.validationError(rowNumber, "Account", "Account number is required"));
        } else {
            String accountValidation = accountValidationService.validateAccountFormat(detail.getAccount());
            if (!AccountValidationService.RESULT_OK.equals(accountValidation)) {
                errors.add(UploadErrorDTO.validationError(rowNumber, "Account", accountValidation));
            }
        }

        if (detail.getAmount() == null || detail.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(UploadErrorDTO.validationError(rowNumber, "Amount", "Amount must be greater than zero"));
        }

        if (detail.getLcyEquivalent() == null || detail.getLcyEquivalent().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(UploadErrorDTO.validationError(rowNumber, "LCY Equivalent", "LCY equivalent must be greater than zero"));
        }

        // Currency validation
        if (detail.getCcyCd() != null) {
            String currencyValidation = accountValidationService.validateCurrencyCode(detail.getCcyCd());
            if (!AccountValidationService.RESULT_OK.equals(currencyValidation)) {
                errors.add(UploadErrorDTO.validationError(rowNumber, "Currency", currencyValidation));
            }
        } else {
            errors.add(UploadErrorDTO.validationError(rowNumber, "Currency", "Currency code is required"));
        }

        // Dr/Cr validation
        if (detail.getDrCr() != null) {
            String drCrValidation = accountValidationService.validateDrCr(detail.getDrCr());
            if (!AccountValidationService.RESULT_OK.equals(drCrValidation)) {
                errors.add(UploadErrorDTO.validationError(rowNumber, "Dr/Cr", drCrValidation));
            }
        } else {
            errors.add(UploadErrorDTO.validationError(rowNumber, "Dr/Cr", "Dr/Cr flag is required"));
        }

        // Transaction code validation
        if (detail.getTxnCode() == null || detail.getTxnCode().trim().isEmpty()) {
            errors.add(UploadErrorDTO.validationError(rowNumber, "Transaction Code", "Transaction code is required"));
        }

        // Stop validation if basic fields are invalid
        if (!errors.isEmpty()) {
            // Add row data to first error for reference
            if (!errors.isEmpty()) {
                UploadErrorDTO firstError = errors.get(0);
                populateErrorData(firstError, detail);
            }
            return errors;
        }

        // Amount format validation for VND (must be integers)
        if ("VND".equals(detail.getCcyCd())) {
            if (!isInteger(detail.getAmount())) {
                errors.add(
                    UploadErrorDTO.amountError(rowNumber, "VND amount must be a whole number", detail.getAmount(), detail.getCcyCd())
                );
            }
            if (!isInteger(detail.getLcyEquivalent())) {
                errors.add(
                    UploadErrorDTO.amountError(
                        rowNumber,
                        "VND LCY equivalent must be a whole number",
                        detail.getLcyEquivalent(),
                        detail.getCcyCd()
                    )
                );
            }
        } else {
            // For foreign currency, only LCY equivalent needs to be integer
            if (!isInteger(detail.getLcyEquivalent())) {
                errors.add(
                    UploadErrorDTO.amountError(rowNumber, "LCY equivalent must be a whole number", detail.getLcyEquivalent(), "VND")
                );
            }
        }

        // Business validation using AccountValidationService
        if (errors.isEmpty() && detail.getAccount() != null) {
            String businessValidation;
            if (detail.getAccount().length() >= 15) {
                // Customer account validation
                businessValidation = accountValidationService.validateCustomerAccount(
                    detail.getRelCust(),
                    detail.getAccount(),
                    detail.getCcyCd(),
                    detail.getAmount(),
                    detail.getDrCr()
                );
            } else {
                // GL account validation
                businessValidation = accountValidationService.validateGLAccount(detail.getAccount());
            }

            if (!AccountValidationService.RESULT_OK.equals(businessValidation)) {
                UploadErrorDTO businessError = UploadErrorDTO.accountError(rowNumber, detail.getAccount(), businessValidation);
                populateErrorData(businessError, detail);
                errors.add(businessError);
            }
        }

        // Populate error data for display
        errors.forEach(error -> populateErrorData(error, detail));

        return errors;
    }

    /**
     * Populate error DTO with data from upload detail
     */
    private void populateErrorData(UploadErrorDTO error, UploadDetail detail) {
        if (detail != null) {
            error.setRelCust(detail.getRelCust());
            error.setAccount(detail.getAccount());
            error.setAccountBranch(detail.getAccountBranch());
            error.setDrCr(detail.getDrCr());
            error.setCcyCd(detail.getCcyCd());
            error.setAmount(detail.getAmount());
            error.setLcyEquivalent(detail.getLcyEquivalent());
            error.setTxnCode(detail.getTxnCode());
            error.setAddlText(detail.getAddlText());
        }
    }

    /**
     * Check if BigDecimal is an integer
     */
    private boolean isInteger(BigDecimal value) {
        if (value == null) return false;
        return value.stripTrailingZeros().scale() <= 0;
    }

    /**
     * Check if row is empty
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int cellNum = 0; cellNum <= COL_ADDL_TEXT; cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String cellValue = getCellStringValue(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get string value from cell
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    } else {
                        // Handle numeric as string (for account numbers, etc.)
                        double numValue = cell.getNumericCellValue();
                        if (numValue == Math.floor(numValue)) {
                            return String.valueOf((long) numValue);
                        } else {
                            return String.valueOf(numValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    // Try to get the cached value
                    try {
                        switch (cell.getCachedFormulaResultType()) {
                            case STRING:
                                return cell.getStringCellValue().trim();
                            case NUMERIC:
                                double numValue = cell.getNumericCellValue();
                                if (numValue == Math.floor(numValue)) {
                                    return String.valueOf((long) numValue);
                                } else {
                                    return String.valueOf(numValue);
                                }
                            default:
                                return cell.getCellFormula();
                        }
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Error reading cell value: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get numeric value from cell
     */
    private BigDecimal getCellNumericValue(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String stringValue = cell.getStringCellValue().trim();
                    if (stringValue.isEmpty()) return null;
                    // Remove any currency symbols or commas
                    stringValue = stringValue.replaceAll("[^0-9.-]", "");
                    return new BigDecimal(stringValue);
                case FORMULA:
                    try {
                        return BigDecimal.valueOf(cell.getNumericCellValue());
                    } catch (Exception e) {
                        return null;
                    }
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid numeric value in cell: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Error reading numeric cell value: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert month number to string
     */
    private String getMonthString(int month) {
        String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };
        return months[month - 1];
    }

    /**
     * Delete batch data
     */
    public boolean deleteBatch(String batchNo) {
        log.info("Attempting to delete batch: {}", batchNo);

        if (batchNo == null || batchNo.trim().isEmpty()) {
            log.warn("Cannot delete batch: batch number is empty");
            return false;
        }

        if (!uploadDetailRepository.existsByBatchNo(batchNo)) {
            log.warn("Cannot delete batch {}: batch does not exist", batchNo);
            return false;
        }

        // Check if batch is already processed
        if (uploadDetailRepository.existsByBatchNoAndUploadStat(batchNo, "Y")) {
            log.error("Cannot delete processed batch: {}", batchNo);
            throw new RuntimeException("Cannot delete processed batch: " + batchNo);
        }

        try {
            uploadDetailRepository.deleteByBatchNo(batchNo);
            log.info("Successfully deleted batch: {}", batchNo);
            return true;
        } catch (Exception e) {
            log.error("Error deleting batch {}: {}", batchNo, e.getMessage());
            throw new RuntimeException("Error deleting batch: " + e.getMessage());
        }
    }

    /**
     * Check if batch exists
     */
    public boolean batchExists(String batchNo) {
        if (batchNo == null || batchNo.trim().isEmpty()) {
            return false;
        }
        return uploadDetailRepository.existsByBatchNo(batchNo);
    }

    /**
     * Get batch summary
     */
    public List<Object[]> getBatchSummary() {
        try {
            return uploadDetailRepository.findBatchSummary();
        } catch (Exception e) {
            log.error("Error getting batch summary: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get batch statistics
     */
    public Object[] getBatchStatistics(String batchNo) {
        try {
            return uploadDetailRepository.getBatchStatistics(batchNo);
        } catch (Exception e) {
            log.error("Error getting batch statistics for {}: {}", batchNo, e.getMessage());
            return null;
        }
    }

    /**
     * Update batch status
     */
    @Transactional
    public void updateBatchStatus(String batchNo, String status) {
        log.info("Updating batch {} status to: {}", batchNo, status);
        try {
            uploadDetailRepository.updateUploadStatusByBatch(batchNo, status);
            log.info("Successfully updated batch {} status to: {}", batchNo, status);
        } catch (Exception e) {
            log.error("Error updating batch {} status: {}", batchNo, e.getMessage());
            throw new RuntimeException("Error updating batch status: " + e.getMessage());
        }
    }
}
