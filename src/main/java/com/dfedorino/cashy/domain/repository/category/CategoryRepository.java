package com.dfedorino.cashy.domain.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CategoryEntity} persistence.
 */
public interface CategoryRepository {

    /**
     * Persists a new category in the database.
     *
     * @param category the {@link CategoryEntity} to create
     * @return the created {@link CategoryEntity} with generated ID
     * @throws RepositoryException if any error occurs
     */
    CategoryEntity createCategory(CategoryEntity category);

    /**
     * Updates the name of an existing category for a specific user.
     *
     * @param userId  the ID of the user who owns the category
     * @param oldName the current name of the category
     * @param newName the new name to set for the category
     * @return an {@link Optional} containing the {@link CategoryEntity} with the new name if found
     * @throws RepositoryException if any error occurs
     */
    Optional<CategoryEntity> updateNameByUserIdAndName(Long userId,
                                                       String oldName,
                                                       String newName);

    /**
     * Updates the limit amount of an existing category for a specific user.
     *
     * @param userId         the ID of the user who owns the category
     * @param name           the name of the category
     * @param newLimitAmount the new limit amount to set for the category
     * @return an {@link Optional} containing the {@link CategoryEntity} with the new limit amount
     * if found
     * @throws RepositoryException if any error occurs
     */
    Optional<CategoryEntity> updateLimitAmountByUserIdAndName(Long userId,
                                                              String name,
                                                              BigDecimal newLimitAmount);

    /**
     * Updates the alert threshold of an existing category for a specific user.
     *
     * @param userId            the ID of the user who owns the category
     * @param name              the name of the category
     * @param newAlertThreshold the new alert threshold to set for the category
     * @return an {@link Optional} containing the {@link CategoryEntity} with the new alert
     * threshold if found
     * @throws RepositoryException if any error occurs
     */
    Optional<CategoryEntity> updateAlertThresholdByUserIdAndName(Long userId,
                                                                 String name,
                                                                 Integer newAlertThreshold);


    /**
     * Retrieves all categories belonging to a specific user.
     *
     * @param userId the user ID
     * @return a list of {@link CategoryEntity} for the given user
     * @throws RepositoryException if any error occurs
     */
    List<CategoryEntity> findByUserId(Long userId);

    /**
     * Retrieves a category belonging to a specific user with the given name.
     *
     * @param userId the user ID
     * @param categoryName the category name
     * @return an {@link Optional} containing the {@link CategoryEntity} if found
     * @throws RepositoryException if any error occurs
     */
    Optional<CategoryEntity> findByUserIdAndName(Long userId, String categoryName);
}

