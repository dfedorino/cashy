package com.dfedorino.cashy.domain.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryBalanceEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CategoryBalanceEntity} persistence. Category balances
 * reflect the remaining budget for a category.
 */
public interface CategoryBalanceRepository {

    /**
     * Persists a new category balance in the database.
     *
     * @param categoryBalance the {@link CategoryBalanceEntity} to create
     * @return the created {@link CategoryBalanceEntity} with generated ID
     * @throws RepositoryException if any error occurs
     */
    CategoryBalanceEntity create(CategoryBalanceEntity categoryBalance);

    /**
     * Updates the remaining balance of a category for a specific user.
     *
     * @param userId     the ID of the user who owns the category
     * @param categoryId the ID of the category whose balance will be updated
     * @param newBalance the new remaining balance to set
     * @return an {@link Optional} containing the updated {@link CategoryBalanceEntity} if the
     * update was successful, or empty if no matching category balance was found
     * @throws RepositoryException if any database error occurs
     */
    Optional<CategoryBalanceEntity> updateBalanceByUserIdAndCategoryId(Long userId,
                                                                       Long categoryId,
                                                                       BigDecimal newBalance);


    /**
     * Finds a category balance by user ID and category ID.
     *
     * @param userId     the ID of the user
     * @param categoryId the ID of the category
     * @return an {@link Optional} containing the {@link CategoryBalanceEntity} if found, or empty
     * if not found
     * @throws RepositoryException if any error occurs
     */
    Optional<CategoryBalanceEntity> findByUserIdAndCategoryId(Long userId, Long categoryId);

    /**
     * Retrieves all category balances for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of {@link CategoryBalanceEntity} for the given user
     * @throws RepositoryException if any error occurs
     */
    List<CategoryBalanceEntity> findByUserId(Long userId);
}

