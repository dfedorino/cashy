package com.dfedorino.cashy.domain.repository.movement;

import com.dfedorino.cashy.domain.model.movement.BalanceMovementEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;

/**
 * Repository interface for managing {@link BalanceMovementEntity} persistence. Balance movements
 * are immutable records representing changes to account and category balances.
 */
public interface BalanceMovementRepository {

    /**
     * Persists a new balance movement in the database.
     *
     * @param movement the {@link BalanceMovementEntity} to create
     * @return the created {@link BalanceMovementEntity} with generated ID
     * @throws RepositoryException if any error occurs
     */
    BalanceMovementEntity createMovement(BalanceMovementEntity movement);
}

