package com.dfedorino.cashy.jdbc.repository.account;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.account.AccountBalanceHistoryEntity;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.account.AccountBalanceHistoryRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JdbcAccountBalanceHistoryRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private AccountBalanceHistoryRepository accountBalanceHistoryRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        accountBalanceHistoryRepository = ctx.getBean(AccountBalanceHistoryRepository.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12.34", "-12.34"})
    void create_account_balance_history_entry(String balance) {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var accountBalanceHistoryEntry = tx(
                () -> accountBalanceHistoryRepository.createHistoryEntry(
                        new AccountBalanceHistoryEntity(user.getId(), new BigDecimal(balance))));

        assertThat(accountBalanceHistoryEntry.getId()).isOne();
        assertThat(accountBalanceHistoryEntry.getUserId()).isEqualTo(user.getId());
        assertThat(accountBalanceHistoryEntry.getBalance())
                .isEqualByComparingTo(new BigDecimal(balance));
        assertThat(accountBalanceHistoryEntry.getCreatedAt()).isNotNull();
    }

    @Test
    void find_account_balance_history_entries() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var accountBalanceHistoryEntry1 = tx(
                () -> accountBalanceHistoryRepository.createHistoryEntry(
                        new AccountBalanceHistoryEntity(user.getId(), new BigDecimal("12.34"))));

        var accountBalanceHistoryEntry2 = tx(
                () -> accountBalanceHistoryRepository.createHistoryEntry(
                        new AccountBalanceHistoryEntity(user.getId(), new BigDecimal("43.21"))));

        var found = tx(() -> accountBalanceHistoryRepository.findByUserId(user.getId()));

        assertThat(found)
                .containsExactlyInAnyOrder(accountBalanceHistoryEntry1,
                                           accountBalanceHistoryEntry2);

    }
}