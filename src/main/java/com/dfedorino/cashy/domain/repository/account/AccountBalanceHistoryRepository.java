package com.dfedorino.cashy.domain.repository.account;

import com.dfedorino.cashy.domain.model.account.AccountBalanceHistoryEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import java.util.List;

/**
 * Repository interface for managing {@link AccountBalanceHistoryEntity} persistence.
 */
public interface AccountBalanceHistoryRepository {

    /**
     * Persists a new account balance history record in the database.
     *
     * @param history the {@link AccountBalanceHistoryEntity} to create
     * @return the created {@link AccountBalanceHistoryEntity} with generated ID
     * @throws RepositoryException if any error occurs
     */
    AccountBalanceHistoryEntity create(AccountBalanceHistoryEntity history);

    /**
     * Retrieves all account balance history records for a given user.
     *
     * @param userId the ID of the user
     * @return a list of {@link AccountBalanceHistoryEntity} sorted by creation time
     * @throws RepositoryException if any error occurs
     */
    List<AccountBalanceHistoryEntity> findByUserId(Long userId);

    /**
     * Retrieves all account balance history records for a given user within a time range.
     *
     * @param userId    the ID of the user
     * @param from      the start timestamp (inclusive)
     * @param to        the end timestamp (inclusive)
     * @return a list of {@link AccountBalanceHistoryEntity} sorted by creation time
     * @throws RepositoryException if any error occurs
     */
    List<AccountBalanceHistoryEntity> findByUserIdAndPeriod(Long userId,
                                                            java.time.LocalDateTime from,
                                                            java.time.LocalDateTime to);
}

