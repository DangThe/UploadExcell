package com.vrbank.uploadexcel.repository;

import com.vrbank.uploadexcel.domain.UploadDetail;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for the UploadDetail entity.
 * Provides CRUD operations and custom queries for upload management
 */
@Repository
public interface UploadDetailRepository extends JpaRepository<UploadDetail, Long> {
    /**
     * Find all upload details by batch number
     * @param batchNo the batch number to search for
     * @return list of upload details for the batch
     */
    List<UploadDetail> findByBatchNo(String batchNo);

    /**
     * Find all upload details by batch number and upload status
     * @param batchNo the batch number
     * @param uploadStat the upload status (Y/N)
     * @return list of upload details matching criteria
     */
    List<UploadDetail> findByBatchNoAndUploadStat(String batchNo, String uploadStat);

    /**
     * Check if batch number exists
     * @param batchNo the batch number to check
     * @return true if batch exists, false otherwise
     */
    boolean existsByBatchNo(String batchNo);

    /**
     * Check if batch number exists with specific upload status
     * Used to check if batch is already processed (uploadStat='Y')
     * @param batchNo the batch number
     * @param uploadStat the upload status to check
     * @return true if batch exists with the status
     */
    boolean existsByBatchNoAndUploadStat(String batchNo, String uploadStat);

    /**
     * Delete all records by batch number
     * Used when user wants to delete an entire batch
     * @param batchNo the batch number to delete
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UploadDetail u WHERE u.batchNo = :batchNo")
    void deleteByBatchNo(@Param("batchNo") String batchNo);

    /**
     * Count records by batch number
     * @param batchNo the batch number
     * @return number of records in the batch
     */
    long countByBatchNo(String batchNo);

    /**
     * Find all batches with their record count
     * Used for batch summary display
     * @return list of arrays containing [batchNo, count]
     */
    @Query("SELECT u.batchNo, COUNT(u) FROM UploadDetail u GROUP BY u.batchNo ORDER BY MAX(u.uploadDate) DESC")
    List<Object[]> findBatchSummary();

    /**
     * Find upload details by branch code
     * @param branchCode the branch code
     * @return list of upload details for the branch
     */
    List<UploadDetail> findByBranchCode(String branchCode);

    /**
     * Find upload details by source code
     * @param sourceCode the source code
     * @return list of upload details for the source
     */
    List<UploadDetail> findBySourceCode(String sourceCode);

    /**
     * Find all upload details that are not processed
     * Used to get pending uploads (uploadStat != 'Y')
     * @return list of unprocessed upload details
     */
    @Query("SELECT u FROM UploadDetail u WHERE u.uploadStat != 'Y' OR u.uploadStat IS NULL")
    List<UploadDetail> findUnprocessedRecords();

    /**
     * Update upload status for all records in a batch
     * Used when marking a batch as processed
     * @param batchNo the batch number
     * @param status the new status to set
     */
    @Modifying
    @Transactional
    @Query("UPDATE UploadDetail u SET u.uploadStat = :status WHERE u.batchNo = :batchNo")
    void updateUploadStatusByBatch(@Param("batchNo") String batchNo, @Param("status") String status);

    /**
     * Find batches uploaded within date range
     * @param startDate start date
     * @param endDate end date
     * @return list of upload details within date range
     */
    @Query("SELECT u FROM UploadDetail u WHERE u.uploadDate BETWEEN :startDate AND :endDate")
    List<UploadDetail> findByUploadDateBetween(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );

    /**
     * Get batch statistics
     * @param batchNo the batch number
     * @return array containing [batchNo, totalCount, successCount, pendingCount]
     */
    @Query(
        """
        SELECT u.batchNo,
               COUNT(u),
               SUM(CASE WHEN u.uploadStat = 'Y' THEN 1 ELSE 0 END),
               SUM(CASE WHEN u.uploadStat = 'N' OR u.uploadStat IS NULL THEN 1 ELSE 0 END)
        FROM UploadDetail u
        WHERE u.batchNo = :batchNo
        GROUP BY u.batchNo
        """
    )
    Object[] getBatchStatistics(@Param("batchNo") String batchNo);

    /**
     * Find upload details by account number
     * @param account the account number
     * @return list of upload details for the account
     */
    List<UploadDetail> findByAccount(String account);

    /**
     * Check if any records exist for a customer
     * @param relCust the customer number
     * @return true if customer has upload records
     */
    boolean existsByRelCust(String relCust);
}
