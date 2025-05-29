package com.vrbank.uploadexcel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for validating accounts and customer information
 * Handles both customer accounts and GL accounts validation
 */
@Service
public class AccountValidationService {

    private final Logger log = LoggerFactory.getLogger(AccountValidationService.class);

    private final JdbcTemplate jdbcTemplate;

    // Constants for validation results
    public static final String RESULT_OK = "OK";
    public static final String AUTH_STAT = "A"; // Authorized status
    public static final String AUTH_STAT_ACCT_DESC = "Account not authorized";
    public static final String AUTH_STAT_CIF_DESC = "Customer not authorized";
    public static final String ACY_AVL_BAL_NOT_DESC = "Insufficient account balance";
    public static final String LCY_EQUIVALENT_INT_DESC = "Amount must be whole number";
    public static final String GL_ACCOUNT_DESC = "GL Account not found";
    public static final String OTHER_DESC = "Validation error";
    public static final String ACCOUNT_NOT_FOUND_DESC = "Account not found";
    public static final String CUSTOMER_NOT_FOUND_DESC = "Customer not found";

    public AccountValidationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Validate customer account
     * Checks account existence, authorization status, and business rules
     */
    public String validateCustomerAccount(String relCust, String account, String ccyCd, BigDecimal amount, String drCr) {
        log.debug("Validating customer account: {}, currency: {}, amount: {}", account, ccyCd, amount);

        try {
            // Query to get account and customer information
            String sql =
                """
                SELECT a.acy_avl_bal, a.auth_stat as auth_stat_acct, c.auth_stat as auth_stat_cif,
                       a.status as account_status, c.status as customer_status
                FROM account_master a
                JOIN customer_master c ON a.customer_no = c.customer_no
                WHERE a.account_no = ? AND a.currency_code = ? AND c.customer_no = ?
                """;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, account, ccyCd, relCust);

            if (results.isEmpty()) {
                log.warn("Account {} with currency {} not found for customer {}", account, ccyCd, relCust);
                return ACCOUNT_NOT_FOUND_DESC;
            }

            Map<String, Object> result = results.get(0);
            String authStatAcct = (String) result.get("auth_stat_acct");
            String authStatCif = (String) result.get("auth_stat_cif");
            String accountStatus = (String) result.get("account_status");
            String customerStatus = (String) result.get("customer_status");
            BigDecimal acyAvlBal = (BigDecimal) result.get("acy_avl_bal");

            // Check if account is active
            if (!"A".equals(accountStatus)) {
                return "Account is not active";
            }

            // Check if customer is active
            if (!"A".equals(customerStatus)) {
                return "Customer is not active";
            }

            // Check if account is authorized
            if (!AUTH_STAT.equals(authStatAcct)) {
                log.warn("Account {} is not authorized. Status: {}", account, authStatAcct);
                return AUTH_STAT_ACCT_DESC;
            }

            // Check if customer is authorized
            if (!AUTH_STAT.equals(authStatCif)) {
                log.warn("Customer {} is not authorized. Status: {}", relCust, authStatCif);
                return AUTH_STAT_CIF_DESC;
            }

            // Validate amount format based on currency
            if ("VND".equals(ccyCd)) {
                // VND amounts must be integers
                if (!isInteger(amount)) {
                    return LCY_EQUIVALENT_INT_DESC;
                }
            }

            // Note: Balance checking is commented out as per original code
            // This was disabled for TT5192 requirement
            /*
            // Check balance for debit transactions
            if ("D".equals(drCr)) {
                if (acyAvlBal.compareTo(amount) <= 0) {
                    log.warn("Insufficient balance for account {}. Available: {}, Required: {}",
                        account, acyAvlBal, amount);
                    return ACY_AVL_BAL_NOT_DESC;
                }
            }
            */

            log.debug("Account validation successful for account: {}", account);
            return RESULT_OK;
        } catch (Exception e) {
            log.error("Error validating customer account {}: {}", account, e.getMessage());
            return OTHER_DESC;
        }
    }

    /**
     * Validate GL account
     * Checks if GL account exists and is active
     */
    public String validateGLAccount(String glAccount) {
        log.debug("Validating GL account: {}", glAccount);

        try {
            // Check if GL account exists and is active
            String sql = "SELECT COUNT(*) FROM gl_master WHERE gl_code = ? AND status = 'A'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, glAccount);

            if (count == null || count == 0) {
                log.warn("GL account {} not found or inactive", glAccount);
                return GL_ACCOUNT_DESC;
            }

            log.debug("GL account validation successful for: {}", glAccount);
            return RESULT_OK;
        } catch (Exception e) {
            log.error("Error validating GL account {}: {}", glAccount, e.getMessage());
            return GL_ACCOUNT_DESC;
        }
    }

    /**
     * Get account balance
     */
    public BigDecimal getAccountBalance(String account, String ccyCd) {
        try {
            String sql = "SELECT acy_avl_bal FROM account_master WHERE account_no = ? AND currency_code = ?";
            BigDecimal balance = jdbcTemplate.queryForObject(sql, BigDecimal.class, account, ccyCd);
            return balance != null ? balance : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error getting account balance for {}: {}", account, e.getMessage());
            return BigDecimal.ZERO;
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
     * Get branches for dropdown
     */
    public List<Map<String, Object>> getBranches() {
        try {
            String sql = "SELECT branch_code, branch_name FROM branch_master WHERE status = 'A' ORDER BY branch_name";
            List<Map<String, Object>> branches = jdbcTemplate.queryForList(sql);
            log.debug("Retrieved {} branches", branches.size());
            return branches;
        } catch (Exception e) {
            log.error("Error getting branches: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get source codes for dropdown
     */
    public List<Map<String, Object>> getSourceCodes() {
        try {
            String sql = "SELECT source_code FROM source_master WHERE status = 'A' ORDER BY source_code";
            List<Map<String, Object>> sourceCodes = jdbcTemplate.queryForList(sql);
            log.debug("Retrieved {} source codes", sourceCodes.size());
            return sourceCodes;
        } catch (Exception e) {
            log.error("Error getting source codes: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get working day for branch
     */
    public LocalDate getWorkingDay(String branchCode) {
        try {
            String sql = "SELECT working_date FROM branch_working_day WHERE branch_code = ? AND status = 'A'";
            java.sql.Date date = jdbcTemplate.queryForObject(sql, java.sql.Date.class, branchCode);

            if (date != null) {
                LocalDate workingDay = date.toLocalDate();
                log.debug("Working day for branch {}: {}", branchCode, workingDay);
                return workingDay;
            }
        } catch (Exception e) {
            log.warn("Error getting working day for branch {}: {}. Using current date.", branchCode, e.getMessage());
        }

        // Fallback to current date if no working day found
        return LocalDate.now();
    }

    /**
     * Validate account number format
     */
    public String validateAccountFormat(String account) {
        if (account == null || account.trim().isEmpty()) {
            return "Account number is required";
        }

        String cleanAccount = account.trim();

        // Customer account: exactly 15 digits
        if (cleanAccount.length() == 15) {
            if (!cleanAccount.matches("\\d{15}")) {
                return "Customer account must be 15 digits";
            }
            return RESULT_OK;
        }

        // GL account: exactly 9 digits
        if (cleanAccount.length() == 9) {
            if (!cleanAccount.matches("\\d{9}")) {
                return "GL account must be 9 digits";
            }
            return RESULT_OK;
        }

        return "Account number must be either 9 digits (GL) or 15 digits (Customer)";
    }

    /**
     * Validate currency code
     */
    public String validateCurrencyCode(String ccyCd) {
        if (ccyCd == null || ccyCd.trim().isEmpty()) {
            return "Currency code is required";
        }

        String cleanCcy = ccyCd.trim().toUpperCase();

        // Basic currency code validation (3 characters)
        if (cleanCcy.length() != 3) {
            return "Currency code must be 3 characters";
        }

        if (!cleanCcy.matches("[A-Z]{3}")) {
            return "Currency code must contain only letters";
        }

        // Check if currency is supported (basic list)
        List<String> supportedCurrencies = List.of("VND", "USD", "EUR", "JPY", "GBP", "AUD", "SGD");
        if (!supportedCurrencies.contains(cleanCcy)) {
            log.warn("Unsupported currency code: {}", cleanCcy);
            // Don't fail validation for unknown currencies, just log warning
        }

        return RESULT_OK;
    }

    /**
     * Validate Dr/Cr flag
     */
    public String validateDrCr(String drCr) {
        if (drCr == null || drCr.trim().isEmpty()) {
            return "Dr/Cr flag is required";
        }

        String cleanDrCr = drCr.trim().toUpperCase();

        if (!"D".equals(cleanDrCr) && !"C".equals(cleanDrCr)) {
            return "Dr/Cr flag must be 'D' (Debit) or 'C' (Credit)";
        }

        return RESULT_OK;
    }

    /**
     * Check if account exists (basic check without authorization)
     */
    public boolean accountExists(String account, String ccyCd) {
        try {
            if (account.length() == 15) {
                // Customer account
                String sql = "SELECT COUNT(*) FROM account_master WHERE account_no = ? AND currency_code = ?";
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, account, ccyCd);
                return count != null && count > 0;
            } else if (account.length() == 9) {
                // GL account
                String sql = "SELECT COUNT(*) FROM gl_master WHERE gl_code = ?";
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, account);
                return count != null && count > 0;
            }
        } catch (Exception e) {
            log.error("Error checking account existence for {}: {}", account, e.getMessage());
        }
        return false;
    }

    /**
     * Get account information for display
     */
    public Map<String, Object> getAccountInfo(String account, String ccyCd) {
        try {
            if (account.length() == 15) {
                // Customer account
                String sql =
                    """
                    SELECT a.account_no, a.currency_code, a.acy_avl_bal,
                           c.customer_name, a.auth_stat, c.auth_stat as customer_auth_stat
                    FROM account_master a
                    JOIN customer_master c ON a.customer_no = c.customer_no
                    WHERE a.account_no = ? AND a.currency_code = ?
                    """;
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, account, ccyCd);
                return results.isEmpty() ? null : results.get(0);
            } else if (account.length() == 9) {
                // GL account
                String sql = "SELECT gl_code, gl_description, status FROM gl_master WHERE gl_code = ?";
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, account);
                return results.isEmpty() ? null : results.get(0);
            }
        } catch (Exception e) {
            log.error("Error getting account info for {}: {}", account, e.getMessage());
        }
        return null;
    }
}
