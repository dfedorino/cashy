package com.dfedorino.cashy.jdbc.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcCategoryRepository implements CategoryRepository {

    public static final String USER_ID = "userId";
    public static final String TRANSACTION_TYPE_ID = "transactionTypeId";
    public static final String NAME = "name";
    public static final String LIMIT_AMOUNT = "limitAmount";
    public static final String ALERT_THRESHOLD = "alertThreshold";
    public static final String OLD_NAME = "oldName";
    public static final String NEW_NAME = "newName";
    private static final String INSERT_CATEGORY =
            "INSERT INTO category("
                    + "user_id, "
                    + "transaction_type_id, "
                    + "name, "
                    + "limit_amount, "
                    + "alert_threshold) "
                    + "VALUES ("
                    + ":" + USER_ID + ", "
                    + ":" + TRANSACTION_TYPE_ID + ", "
                    + ":" + NAME + ", "
                    + ":" + LIMIT_AMOUNT + ", "
                    + ":" + ALERT_THRESHOLD + ")";

    private static final String UPDATE_NAME =
            "UPDATE category SET name = :" + NEW_NAME
                    + " WHERE user_id = :" + USER_ID + " AND name = :" + OLD_NAME;
    private static final String UPDATE_LIMIT_AMOUNT =
            "UPDATE category SET limit_amount = :" + LIMIT_AMOUNT
                    + " WHERE user_id = :" + USER_ID + " AND name = :" + NAME;
    private static final String UPDATE_ALERT_THRESHOLD =
            "UPDATE category SET alert_threshold = :" + ALERT_THRESHOLD
                    + " WHERE user_id = :" + USER_ID + " AND name = :" + NAME;
    private static final String SELECT_BY_USER_ID =
            "SELECT * FROM category WHERE user_id = :" + USER_ID;

    private final JdbcClient jdbcClient;

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_CATEGORY)
                    .param(USER_ID, category.getUserId())
                    .param(TRANSACTION_TYPE_ID, category.getTransactionTypeId())
                    .param(NAME, category.getName())
                    .param(LIMIT_AMOUNT, category.getLimitAmount())
                    .param(ALERT_THRESHOLD, category.getAlertThreshold())
                    .update(keyHolder);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        category.setId(KeyHolderUtil.getId(keyHolder));
        category.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));
        return category;
    }

    @Override
    public Optional<CategoryEntity> updateNameByUserIdAndName(Long userId, String oldName,
                                                              String newName) {
        try {
            int updated = jdbcClient.sql(UPDATE_NAME)
                    .param(USER_ID, userId)
                    .param(OLD_NAME, oldName)
                    .param(NEW_NAME, newName)
                    .update();

            if (updated == 0) {
                return Optional.empty();
            }

            return findByUserIdAndName(userId, newName);

        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<CategoryEntity> updateLimitAmountByUserIdAndName(Long userId,
                                                                     String name,
                                                                     BigDecimal newLimitAmount) {
        try {
            int updated = jdbcClient.sql(UPDATE_LIMIT_AMOUNT)
                    .param(USER_ID, userId)
                    .param(NAME, name)
                    .param(LIMIT_AMOUNT, newLimitAmount)
                    .update();

            if (updated == 0) {
                return Optional.empty();
            }

            return findByUserIdAndName(userId, name);

        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<CategoryEntity> updateAlertThresholdByUserIdAndName(Long userId, String name,
                                                                        Integer newAlertThreshold) {
        try {
            int updated = jdbcClient.sql(UPDATE_ALERT_THRESHOLD)
                    .param(USER_ID, userId)
                    .param(NAME, name)
                    .param(ALERT_THRESHOLD, newAlertThreshold)
                    .update();

            if (updated == 0) {
                return Optional.empty();
            }

            return findByUserIdAndName(userId, name);

        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<CategoryEntity> findByUserId(Long userId) {
        try {
            return jdbcClient.sql(SELECT_BY_USER_ID)
                    .param(USER_ID, userId)
                    .query(CategoryEntity.class)
                    .list();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<CategoryEntity> findByUserIdAndName(Long userId, String categoryName) {
        try {
            return jdbcClient.sql(SELECT_BY_USER_ID + " AND name = :" + NAME)
                    .param(USER_ID, userId)
                    .param(NAME, categoryName)
                    .query(CategoryEntity.class)
                    .optional();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }
}


