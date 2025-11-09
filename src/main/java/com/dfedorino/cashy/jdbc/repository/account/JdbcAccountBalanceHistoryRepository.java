package com.dfedorino.cashy.jdbc.repository.account;

import com.dfedorino.cashy.domain.model.account.AccountBalanceHistoryEntity;
import com.dfedorino.cashy.domain.repository.account.AccountBalanceHistoryRepository;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcAccountBalanceHistoryRepository implements AccountBalanceHistoryRepository {

    public static final String USER_ID = "userId";
    public static final String BALANCE = "balance";
    private static final String INSERT_HISTORY =
            "INSERT INTO account_balance_history(user_id, balance) "
                    + "VALUES (:" + USER_ID + ", :" + BALANCE + ")";
    private static final String SELECT_BY_USER = "SELECT * FROM account_balance_history "
            + "WHERE user_id = :" + USER_ID
            + " ORDER BY created_at ASC";

    private final JdbcClient jdbcClient;

    @Override
    public AccountBalanceHistoryEntity createHistoryEntry(AccountBalanceHistoryEntity history) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_HISTORY)
                    .param(USER_ID, history.getUserId())
                    .param(BALANCE, history.getBalance())
                    .update(keyHolder);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        history.setId(KeyHolderUtil.getId(keyHolder));
        history.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));
        return history;
    }

    @Override
    public List<AccountBalanceHistoryEntity> findByUserId(Long userId) {
        try {
            return jdbcClient.sql(SELECT_BY_USER)
                    .param(USER_ID, userId)
                    .query(AccountBalanceHistoryEntity.class)
                    .list();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }
}

