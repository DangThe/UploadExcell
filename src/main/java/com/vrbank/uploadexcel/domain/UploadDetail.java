package com.vrbank.uploadexcel.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity for storing Excel upload details
 * Represents each row of data uploaded from Excel files
 */
@Entity
@Table(name = "detb_upload_detail")
public class UploadDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 20)
    @Column(name = "batch_no", length = 20, nullable = false)
    private String batchNo;

    @NotNull
    @Size(max = 10)
    @Column(name = "branch_code", length = 10, nullable = false)
    private String branchCode;

    @NotNull
    @Size(max = 10)
    @Column(name = "source_code", length = 10, nullable = false)
    private String sourceCode;

    @Size(max = 20)
    @Column(name = "rel_cust", length = 20)
    private String relCust;

    @NotNull
    @Size(max = 20)
    @Column(name = "account", length = 20, nullable = false)
    private String account;

    @NotNull
    @Size(max = 10)
    @Column(name = "account_branch", length = 10, nullable = false)
    private String accountBranch;

    @NotNull
    @Size(max = 1)
    @Column(name = "dr_cr", length = 1, nullable = false)
    private String drCr;

    @NotNull
    @Size(max = 3)
    @Column(name = "ccy_cd", length = 3, nullable = false)
    private String ccyCd;

    @NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(name = "lcy_equivalent", precision = 21, scale = 2, nullable = false)
    private BigDecimal lcyEquivalent;

    @NotNull
    @Size(max = 10)
    @Column(name = "txn_code", length = 10, nullable = false)
    private String txnCode;

    @Size(max = 200)
    @Column(name = "addl_text", length = 200)
    private String addlText;

    @NotNull
    @Column(name = "exch_rate", precision = 15, scale = 6, nullable = false)
    private BigDecimal exchRate;

    @NotNull
    @Column(name = "initiation_date")
    private LocalDate initiationDate;

    @NotNull
    @Column(name = "value_date")
    private LocalDate valueDate;

    @NotNull
    @Column(name = "upload_date")
    private LocalDate uploadDate;

    @Size(max = 10)
    @Column(name = "fin_cycle", length = 10)
    private String finCycle;

    @Size(max = 10)
    @Column(name = "period_code", length = 10)
    private String periodCode;

    @Size(max = 10)
    @Column(name = "curr_no", length = 10)
    private String currNo;

    @Size(max = 1)
    @Column(name = "upload_stat", length = 1, columnDefinition = "varchar(1) default 'N'")
    private String uploadStat = "N";

    @Size(max = 1)
    @Column(name = "delete_stat", length = 1, columnDefinition = "varchar(1) default 'N'")
    private String deleteStat = "N";

    // Constructors
    public UploadDetail() {}

    public UploadDetail(String batchNo, String branchCode, String sourceCode) {
        this.batchNo = batchNo;
        this.branchCode = branchCode;
        this.sourceCode = sourceCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getExchRate() {
        return exchRate;
    }

    public void setExchRate(BigDecimal exchRate) {
        this.exchRate = exchRate;
    }

    public LocalDate getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(LocalDate initiationDate) {
        this.initiationDate = initiationDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFinCycle() {
        return finCycle;
    }

    public void setFinCycle(String finCycle) {
        this.finCycle = finCycle;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public String getCurrNo() {
        return currNo;
    }

    public void setCurrNo(String currNo) {
        this.currNo = currNo;
    }

    public String getUploadStat() {
        return uploadStat;
    }

    public void setUploadStat(String uploadStat) {
        this.uploadStat = uploadStat;
    }

    public String getDeleteStat() {
        return deleteStat;
    }

    public void setDeleteStat(String deleteStat) {
        this.deleteStat = deleteStat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadDetail)) return false;
        UploadDetail that = (UploadDetail) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "UploadDetail{" +
            "id=" +
            getId() +
            ", batchNo='" +
            getBatchNo() +
            "'" +
            ", branchCode='" +
            getBranchCode() +
            "'" +
            ", sourceCode='" +
            getSourceCode() +
            "'" +
            ", account='" +
            getAccount() +
            "'" +
            ", amount=" +
            getAmount() +
            ", ccyCd='" +
            getCcyCd() +
            "'" +
            "}"
        );
    }
}
