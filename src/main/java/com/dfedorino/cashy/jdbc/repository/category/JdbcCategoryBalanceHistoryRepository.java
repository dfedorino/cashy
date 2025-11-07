package com.dfedorino.cashy.jdbc.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryBalanceHistoryEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceHistoryRepository;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcCategoryBalanceHistoryRepository implements CategoryBalanceHistoryRepository {

    public static final String USER_ID = "userId";
    public static final String CATEGORY_ID = "categoryId";
    public static final String REMAINING_BALANCE = "remainingBalance";
    public static final String CREATED_AT = "createdAt";

    private final JdbcClient jdbcClient;

    private static final String INSERT_HISTORY = "INSERT INTO category_balance_history(user_id, category_id, remaining_balance) "
            + "VALUES (:" + USER_ID + ", :" + CATEGORY_ID + ", :" + REMAINING_BALANCE + ")";

    private static final String SELECT_BY_USER_AND_CATEGORY = "SELECT * FROM category_balance_history "
            + "WHERE user_id = :" + USER_ID + " AND category_id = :" + CATEGORY_ID
            + " ORDER BY created_at ASC";

    private static final String SELECT_BY_USER_AND_CATEGORY_BETWEEN = "SELECT * FROM category_balance_history "
            + "WHERE user_id = :" + USER_ID + " AND category_id = :" + CATEGORY_ID
            + " AND created_at BETWEEN :" + CREATED_AT + "_from AND :" + CREATED_AT + "_to"
            + " ORDER BY created_at ASC";

    @Override
    public CategoryBalanceHistoryEntity createHistoryEntry(CategoryBalanceHistoryEntity history) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_HISTORY)
                    .param(USER_ID, history.getUserId())
                    .param(CATEGORY_ID, history.getCategoryId())
                    .param(REMAINING_BALANCE, history.getRemainingBalance())
                    .update(keyHolder);
        } catch (Exception e) {
            log.error(">> Failed to create category balance history for userId: {}, categoryId: {}",
                      history.getUserId(), history.getCategoryId());
            log.error(">> ", e);
            throw new RepositoryException(e);
        }

        history.setId(KeyHolderUtil.getId(keyHolder));
        history.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));
        return history;
    }

    @Override
    public List<CategoryBalanceHistoryEntity> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        try {
            return jdbcClient.sql(SELECT_BY_USER_AND_CATEGORY)
                    .param(USER_ID, userId)
                    .param(CATEGORY_ID, categoryId)
                    .query(CategoryBalanceHistoryEntity.class)
                    .list();
        } catch (Exception e) {
            log.error(">> Failed to fetch category balance history for userId: {}, categoryId: {}",
                      userId, categoryId);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<CategoryBalanceHistoryEntity> findByUserIdAndCategoryIdBetween(Long userId,
                                                                               Long categoryId,
                                                                               LocalDateTime from,
                                                                               LocalDateTime to) {
        try {
            return jdbcClient.sql(SELECT_BY_USER_AND_CATEGORY_BETWEEN)
                    .param(USER_ID, userId)
                    .param(CATEGORY_ID, categoryId)
                    .param(CREATED_AT + "_from", from)
                    .param(CREATED_AT + "_to", to)
                    .query(CategoryBalanceHistoryEntity.class)
                    .list();
        } catch (Exception e) {
            log.error(">> Failed to fetch category balance history for userId: {}, categoryId: {} between {} and {}",
                      userId, categoryId, from, to);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }
}

