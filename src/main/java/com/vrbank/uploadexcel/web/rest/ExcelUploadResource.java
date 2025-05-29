package com.vrbank.uploadexcel.web.rest;

import com.vrbank.uploadexcel.service.AccountValidationService;
import com.vrbank.uploadexcel.service.ExcelUploadService;
import com.vrbank.uploadexcel.service.dto.ExcelUploadDTO;
import com.vrbank.uploadexcel.service.dto.UploadResultDTO;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing Excel file uploads
 */
@RestController
@RequestMapping("/api/excel-upload")
public class ExcelUploadResource {

    private final Logger log = LoggerFactory.getLogger(ExcelUploadResource.class);

    private final ExcelUploadService excelUploadService;
    private final AccountValidationService accountValidationService;

    public ExcelUploadResource(ExcelUploadService excelUploadService, AccountValidationService accountValidationService) {
        this.excelUploadService = excelUploadService;
        this.accountValidationService = accountValidationService;
    }

    /**
     * POST /api/excel-upload : Upload Excel file
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResultDTO> uploadExcelFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("batchNo") String batchNo,
        @RequestParam("branchCode") String branchCode,
        @RequestParam("sourceCode") String sourceCode,
        @RequestParam("exchRate") String exchRate,
        @RequestParam("entryDate") String entryDate
    ) {
        log.debug("REST request to upload Excel file for batch: {}", batchNo);

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResult("File is empty", batchNo));
            }

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
                return ResponseEntity.badRequest()
                    .body(createErrorResult("Invalid file format. Only .xlsx and .xls files are supported", batchNo));
            }

            // Create upload parameters
            ExcelUploadDTO uploadParams = new ExcelUploadDTO();
            uploadParams.setBatchNo(batchNo.trim());
            uploadParams.setBranchCode(branchCode.trim());
            uploadParams.setSourceCode(sourceCode.trim());
            uploadParams.setExchRate(new java.math.BigDecimal(exchRate));
            uploadParams.setEntryDate(java.time.LocalDate.parse(entryDate));

            // Process upload
            UploadResultDTO result = excelUploadService.processExcelUpload(file, uploadParams);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
            }
        } catch (Exception e) {
            log.error("Error uploading Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResult("Error processing file: " + e.getMessage(), batchNo)
            );
        }
    }

    /**
     * DELETE /api/excel-upload/batch/{batchNo} : Delete batch data
     */
    @DeleteMapping("/batch/{batchNo}")
    public ResponseEntity<Map<String, Object>> deleteBatch(@PathVariable String batchNo) {
        log.debug("REST request to delete batch: {}", batchNo);

        try {
            if (batchNo == null || batchNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Batch number is required"));
            }

            if (!excelUploadService.batchExists(batchNo)) {
                return ResponseEntity.ok().body(Map.of("success", false, "message", "Batch " + batchNo + " does not exist"));
            }

            boolean deleted = excelUploadService.deleteBatch(batchNo);

            if (deleted) {
                return ResponseEntity.ok().body(Map.of("success", true, "message", "Batch " + batchNo + " deleted successfully"));
            } else {
                return ResponseEntity.ok().body(Map.of("success", false, "message", "Failed to delete batch " + batchNo));
            }
        } catch (RuntimeException e) {
            log.error("Error deleting batch: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Error deleting batch: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/excel-upload/batch/{batchNo}/exists : Check if batch exists
     */
    @GetMapping("/batch/{batchNo}/exists")
    public ResponseEntity<Map<String, Object>> checkBatchExists(@PathVariable String batchNo) {
        log.debug("REST request to check if batch exists: {}", batchNo);

        boolean exists = excelUploadService.batchExists(batchNo);
        return ResponseEntity.ok(Map.of("exists", exists, "batchNo", batchNo));
    }

    /**
     * GET /api/excel-upload/batches : Get batch summary
     */
    @GetMapping("/batches")
    public ResponseEntity<List<Object[]>> getBatchSummary() {
        log.debug("REST request to get batch summary");

        List<Object[]> batches = excelUploadService.getBatchSummary();
        return ResponseEntity.ok(batches);
    }

    /**
     * GET /api/excel-upload/branches : Get branches for dropdown
     */
    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, Object>>> getBranches() {
        log.debug("REST request to get branches");

        List<Map<String, Object>> branches = accountValidationService.getBranches();
        return ResponseEntity.ok(branches);
    }

    /**
     * GET /api/excel-upload/source-codes : Get source codes for dropdown
     */
    @GetMapping("/source-codes")
    public ResponseEntity<List<Map<String, Object>>> getSourceCodes() {
        log.debug("REST request to get source codes");

        List<Map<String, Object>> sourceCodes = accountValidationService.getSourceCodes();
        return ResponseEntity.ok(sourceCodes);
    }

    /**
     * GET /api/excel-upload/working-day/{branchCode} : Get working day for branch
     */
    @GetMapping("/working-day/{branchCode}")
    public ResponseEntity<Map<String, Object>> getWorkingDay(@PathVariable String branchCode) {
        log.debug("REST request to get working day for branch: {}", branchCode);

        java.time.LocalDate workingDay = accountValidationService.getWorkingDay(branchCode);
        return ResponseEntity.ok(Map.of("workingDay", workingDay.toString(), "branchCode", branchCode));
    }

    /**
     * GET /api/excel-upload/template : Download Excel template
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        log.debug("REST request to download Excel template");

        try {
            // Create a simple template
            byte[] template = createExcelTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "upload_template.xlsx");

            return ResponseEntity.ok().headers(headers).body(template);
        } catch (Exception e) {
            log.error("Error creating Excel template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create error result DTO
     */
    private UploadResultDTO createErrorResult(String message, String batchNo) {
        UploadResultDTO result = new UploadResultDTO();
        result.setSuccess(false);
        result.setMessage(message);
        result.setBatchNo(batchNo);
        result.setTotalRows(0);
        result.setSuccessCount(0);
        result.setErrorCount(0);
        result.setErrors(List.of());
        return result;
    }

    /**
     * Create Excel template
     */
    private byte[] createExcelTemplate() throws Exception {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Upload Data");

            // Create header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {
                "STT",
                "REL_CUST",
                "ACCOUNT",
                "ACCOUNT_BRANCH",
                "DR_CR",
                "CCY_CD",
                "AMOUNT",
                "LCY_EQUIVALENT",
                "TXN_CODE",
                "ADDL_TEXT",
            };

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create description row
            org.apache.poi.ss.usermodel.Row descRow = sheet.createRow(1);
            String[] descriptions = {
                "Sequence",
                "Customer No",
                "Account No",
                "Account Branch",
                "Dr/Cr (D/C)",
                "Currency",
                "Amount",
                "LCY Equivalent",
                "Transaction Code",
                "Additional Text",
            };

            for (int i = 0; i < descriptions.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = descRow.createCell(i);
                cell.setCellValue(descriptions[i]);
            }

            // Create sample data row
            org.apache.poi.ss.usermodel.Row sampleRow = sheet.createRow(2);
            Object[] sampleData = {
                1,
                "1234567890",
                "123456789012345",
                "001",
                "D",
                "VND",
                1000000,
                1000000,
                "TXN001",
                "Sample transaction",
            };

            for (int i = 0; i < sampleData.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = sampleRow.createCell(i);
                if (sampleData[i] instanceof Number) {
                    cell.setCellValue(((Number) sampleData[i]).doubleValue());
                } else {
                    cell.setCellValue(sampleData[i].toString());
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
