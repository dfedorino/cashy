package com.dfedorino.cashy.domain.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryBalanceHistoryEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import java.time.LocalDateTime;
import java.util.List;

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
     * Retrieves all historical category balance records for a specific user and category.
     *
     * @param userId     the ID of the user
     * @param categoryId the ID of the category
     * @return a list of {@link CategoryBalanceHistoryEntity} ordered by creation time ascending
     * @throws RepositoryException if any error occurs
     */
    List<CategoryBalanceHistoryEntity> findByUserIdAndCategoryId(Long userId, Long categoryId);

    /**
     * Retrieves historical category balance records for a specific user and category within a time
     * range.
     *
     * @param userId     the ID of the user
     * @param categoryId the ID of the category
     * @param from       start of the time range (inclusive)
     * @param to         end of the time range (inclusive)
     * @return a list of {@link CategoryBalanceHistoryEntity} ordered by creation time ascending
     * @throws RepositoryException if any error occurs
     */
    List<CategoryBalanceHistoryEntity> findByUserIdAndCategoryIdBetween(Long userId,
                                                                        Long categoryId,
                                                                        LocalDateTime from,
                                                                        LocalDateTime to);
}

