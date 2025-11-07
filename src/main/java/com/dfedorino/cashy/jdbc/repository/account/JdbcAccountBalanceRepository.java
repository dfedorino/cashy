package com.dfedorino.cashy.jdbc.repository.account;

import com.dfedorino.cashy.domain.model.account.AccountBalanceEntity;
import com.dfedorino.cashy.domain.repository.account.AccountBalanceRepository;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcAccountBalanceRepository implements AccountBalanceRepository {

    public static final String USER_ID = "userId";
    public static final String BALANCE = "balance";
    private static final String INSERT_ACCOUNT_BALANCE =
            "INSERT INTO account_balance(user_id, balance) "
                    + "VALUES (:" + USER_ID + ", :" + BALANCE + ")";
    private static final String UPDATE_BALANCE_BY_USER = "UPDATE account_balance "
            + "SET balance = :" + BALANCE + ", updated_at = CURRENT_TIMESTAMP "
            + "WHERE user_id = :" + USER_ID;
    private static final String SELECT_BY_USER = "SELECT * FROM account_balance "
            + "WHERE user_id = :" + USER_ID;
    private final JdbcClient jdbcClient;

    @Override
    public AccountBalanceEntity create(AccountBalanceEntity accountBalance) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_ACCOUNT_BALANCE)
                    .param(USER_ID, accountBalance.getUserId())
                    .param(BALANCE, accountBalance.getBalance())
                    .update(keyHolder);
        } catch (Exception e) {
            log.error(">> Failed to create account balance for userId: {}",
                      accountBalance.getUserId());
            log.error(">> ", e);
            throw new RepositoryException(e);
        }

        accountBalance.setId(KeyHolderUtil.getId(keyHolder));
        accountBalance.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));
        return accountBalance;
    }

    @Override
    public Optional<AccountBalanceEntity> updateBalanceByUserId(Long userId,
                                                                BigDecimal newBalance) {
        try {
            int updated = jdbcClient.sql(UPDATE_BALANCE_BY_USER)
                    .param(BALANCE, newBalance)
                    .param(USER_ID, userId)
                    .update();

            if (updated == 0) {
                return Optional.empty();
            }

            // Fetch the updated entity
            return findByUserId(userId);
        } catch (Exception e) {
            log.error(">> Failed to update account balance for userId: {}, newBalance: {}", userId,
                      newBalance);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<AccountBalanceEntity> findByUserId(Long userId) {
        try {
            return jdbcClient.sql(SELECT_BY_USER)
                    .param(USER_ID, userId)
                    .query(AccountBalanceEntity.class)
                    .optional();
        } catch (Exception e) {
            log.error(">> Failed to find account balance for userId: {}", userId);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }
}

