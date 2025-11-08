package com.dfedorino.cashy.jdbc.repository.account;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.account.AccountBalanceEntity;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.account.AccountBalanceRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.math.BigDecimal;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JdbcAccountBalanceRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private AccountBalanceRepository accountBalanceRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        accountBalanceRepository = ctx.getBean(AccountBalanceRepository.class);
    }

    @Test
    void create_account_balance() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var accountBalance = tx(() -> accountBalanceRepository.create(new AccountBalanceEntity(
                user.getId(),
                BigDecimal.ZERO
        )));

        assertThat(accountBalance.getId()).isOne();
        assertThat(accountBalance.getUserId()).isEqualTo(user.getId());
        assertThat(accountBalance.getBalance()).isZero();
        assertThat(accountBalance.getCreatedAt()).isNotNull();
        assertThat(accountBalance.getUpdatedAt()).isNotNull();
        assertThat(accountBalance.getCreatedAt()).isEqualTo(accountBalance.getUpdatedAt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12.34", "-12.34"})
    void update_balance(String newBalance) {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var createdBalance = tx(() -> accountBalanceRepository.create(new AccountBalanceEntity(
                user.getId(),
                BigDecimal.ZERO)));

        var updatedAccountBalance = tx(() -> accountBalanceRepository.updateBalanceByUserId(
                user.getId(),
                new BigDecimal(newBalance))
        );

        assertThat(updatedAccountBalance).isNotEmpty();

        assertThat(updatedAccountBalance.get())
                .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                                                  .withIgnoredFields("balance",
                                                                     "updatedAt")
                                                  .build())
                .isEqualTo(createdBalance);

        assertThat(updatedAccountBalance.get().getBalance())
                .isEqualByComparingTo(new BigDecimal(newBalance));
    }

    @Test
    void findByUserId() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var createdBalance = tx(() -> accountBalanceRepository.create(new AccountBalanceEntity(
                user.getId(),
                BigDecimal.ZERO)));

        var foundAccountBalance = tx(() -> accountBalanceRepository.findByUserId(user.getId()));

        assertThat(foundAccountBalance).isNotEmpty();
        assertThat(foundAccountBalance.get())
                .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                                                  .withComparatorForType(BigDecimal::compareTo,
                                                                         BigDecimal.class)
                                                  .build())
                .isEqualTo(createdBalance);
    }
}