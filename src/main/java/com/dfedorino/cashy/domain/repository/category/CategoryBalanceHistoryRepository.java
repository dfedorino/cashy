package com.dfedorino.cashy.domain.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryBalanceHistoryEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import java.util.List;
import java.util.Set;

/**
 * Repository interface for managing {@link CategoryBalanceHistoryEntity} persistence. History
 * records are immutable and represent snapshots of category balances over time.
 */
public interface CategoryBalanceHistoryRepository {

    /**
     * Persists a new category balance history record in the database.
     *
     * @param history the {@link CategoryBalanceHistoryEntity} to create
     * @return the created {@link CategoryBalanceHistoryEntity} with generated ID
     * @throws RepositoryException if any error occurs
     */
    CategoryBalanceHistoryEntity createHistoryEntry(CategoryBalanceHistoryEntity history);

    /**
     * Retrieves all historical category balance records for a specific user and categories.
     *
     * @param userId      the ID of the user
     * @param categoryIds the IDs of the categories
     * @return a list of {@link CategoryBalanceHistoryEntity} ordered by creation time ascending
     * @throws RepositoryException if any error occurs
     */
    List<CategoryBalanceHistoryEntity> findByUserIdAndCategoryIds(Long userId,
                                                                  Set<Long> categoryIds);
}

