package com.dfedorino.cashy.jdbc.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryBalanceEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceRepository;
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
public class JdbcCategoryBalanceRepository implements CategoryBalanceRepository {

    public static final String USER_ID = "userId";
    public static final String CATEGORY_ID = "categoryId";
    public static final String REMAINING_BALANCE = "remainingBalance";
    private static final String INSERT_CATEGORY_BALANCE =
            "INSERT INTO category_balance(user_id, category_id, remaining_balance) "
                    + "VALUES (:" + USER_ID + ", :" + CATEGORY_ID + ", :" + REMAINING_BALANCE + ")";
    private static final String UPDATE_BALANCE =
            "UPDATE category_balance SET remaining_balance = :" + REMAINING_BALANCE
                    + " WHERE user_id = :" + USER_ID + " AND category_id = :" + CATEGORY_ID;
    private static final String SELECT_BY_USER_AND_CATEGORY = "SELECT * FROM category_balance "
            + "WHERE user_id = :" + USER_ID + " AND category_id = :" + CATEGORY_ID;
    private static final String SELECT_BY_USER =
            "SELECT * FROM category_balance WHERE user_id = :" + USER_ID;
    private final JdbcClient jdbcClient;

    @Override
    public CategoryBalanceEntity create(CategoryBalanceEntity categoryBalance) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_CATEGORY_BALANCE)
                    .param(USER_ID, categoryBalance.getUserId())
                    .param(CATEGORY_ID, categoryBalance.getCategoryId())
                    .param(REMAINING_BALANCE, categoryBalance.getRemainingBalance())
                    .update(keyHolder);
        } catch (Exception e) {
            log.error(">> Failed to create category balance for userId: {}, categoryId: {}",
                      categoryBalance.getUserId(), categoryBalance.getCategoryId());
            log.error(">> ", e);
            throw new RepositoryException(e);
        }

        categoryBalance.setId(KeyHolderUtil.getId(keyHolder));
        categoryBalance.setUpdatedAt(KeyHolderUtil.getCreatedAt(keyHolder));
        return categoryBalance;
    }

    @Override
    public Optional<CategoryBalanceEntity> updateBalanceByUserIdAndCategoryId(Long userId,
                                                                              Long categoryId,
                                                                              BigDecimal newBalance) {
        try {
            int updated = jdbcClient.sql(UPDATE_BALANCE)
                    .param(USER_ID, userId)
                    .param(CATEGORY_ID, categoryId)
                    .param(REMAINING_BALANCE, newBalance)
                    .update();

            if (updated == 0) {
                return Optional.empty();
            }

            return jdbcClient.sql(SELECT_BY_USER_AND_CATEGORY)
                    .param(USER_ID, userId)
                    .param(CATEGORY_ID, categoryId)
                    .query(CategoryBalanceEntity.class)
                    .optional();

        } catch (Exception e) {
            log.error(
                    ">> Failed to update remaining balance for userId: {}, categoryId: {}, newBalance: {}",
                    userId, categoryId, newBalance);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<CategoryBalanceEntity> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        try {
            return jdbcClient.sql(SELECT_BY_USER_AND_CATEGORY)
                    .param(USER_ID, userId)
                    .param(CATEGORY_ID, categoryId)
                    .query(CategoryBalanceEntity.class)
                    .optional();
        } catch (Exception e) {
            log.error(">> Failed to find category balance for userId: {}, categoryId: {}", userId,
                      categoryId);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<CategoryBalanceEntity> findByUserId(Long userId) {
        try {
            return jdbcClient.sql(SELECT_BY_USER)
                    .param(USER_ID, userId)
                    .query(CategoryBalanceEntity.class)
                    .list();
        } catch (Exception e) {
            log.error(">> Failed to fetch category balances for userId: {}", userId);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }
}

