package com.dfedorino.cashy.domain.repository.operation;

import com.dfedorino.cashy.domain.model.operation.OperationEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;

/**
 * Repository interface for managing {@link OperationEntity} persistence. Operations are immutable
 * once created.
 */
public interface OperationRepository {

    /**
     * Persists a new operation in the database.
     *
     * @param operation the {@link OperationEntity} to create
     * @return the created {@link OperationEntity} with generated ID
     * @throws RepositoryException if a database error occurs
     */
    OperationEntity createOperation(OperationEntity operation);
}

