package com.dfedorino.cashy.jdbc.repository.category;

import com.dfedorino.cashy.domain.model.category.CategoryBalanceHistoryEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceHistoryRepository;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import java.util.List;
import java.util.Set;
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
    public static final String CATEGORY_IDS = "categoryIds";
    public static final String CURRENT_BALANCE = "currentBalance";
    public static final String REMAINING_BALANCE = "remainingBalance";
    private static final String INSERT_HISTORY = "INSERT INTO category_balance_history("
            + "user_id, "
            + "category_id, "
            + "current_balance, "
            + "remaining_balance"
            + ") "
            + "VALUES ("
            + ":" + USER_ID + ", "
            + ":" + CATEGORY_IDS + ", "
            + ":" + CURRENT_BALANCE + ", "
            + ":" + REMAINING_BALANCE + ")";
    private static final String SELECT_BY_USER_AND_CATEGORIES =
            "SELECT * FROM category_balance_history "
                    + "WHERE user_id = :" + USER_ID + " AND category_id in (:" + CATEGORY_IDS + ") "
                    + "ORDER BY created_at ASC";
    private final JdbcClient jdbcClient;

    @Override
    public CategoryBalanceHistoryEntity createHistoryEntry(CategoryBalanceHistoryEntity history) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_HISTORY)
                    .param(USER_ID, history.getUserId())
                    .param(CATEGORY_IDS, history.getCategoryId())
                    .param(CURRENT_BALANCE, history.getCurrentBalance())
                    .param(REMAINING_BALANCE, history.getRemainingBalance())
                    .update(keyHolder);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        history.setId(KeyHolderUtil.getId(keyHolder));
        history.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));
        return history;
    }

    @Override
    public List<CategoryBalanceHistoryEntity> findByUserIdAndCategoryIds(Long userId,
                                                                         Set<Long> categoryIds) {
        try {
            return jdbcClient.sql(SELECT_BY_USER_AND_CATEGORIES)
                    .param(USER_ID, userId)
                    .param(CATEGORY_IDS, categoryIds)
                    .query(CategoryBalanceHistoryEntity.class)
                    .list();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }
}

