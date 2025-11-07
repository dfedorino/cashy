package com.dfedorino.cashy.domain.repository.account;

import com.dfedorino.cashy.domain.model.account.AccountBalanceEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository interface for managing {@link AccountBalanceEntity} persistence.
 */
public interface AccountBalanceRepository {

    /**
     * Persists a new account balance in the database.
     *
     * @param accountBalance the {@link AccountBalanceEntity} to create
     * @return the created {@link AccountBalanceEntity} with generated ID
     * @throws RepositoryException if any error occurs
     */
    AccountBalanceEntity create(AccountBalanceEntity accountBalance);

    /**
     * Updates the balance of an existing account.
     *
     * @param userId     the ID of the user whose account balance will be updated
     * @param newBalance the new balance to set
     * @return an {@link Optional} containing the updated {@link AccountBalanceEntity} if found
     * @throws RepositoryException if any error occurs
     */
    Optional<AccountBalanceEntity> updateBalanceByUserId(Long userId, BigDecimal newBalance);

    /**
     * Finds an account balance by user ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the {@link AccountBalanceEntity} if found, or empty if
     * not found
     * @throws RepositoryException if any error occurs
     */
    Optional<AccountBalanceEntity> findByUserId(Long userId);
}

