package com.dfedorino.cashy.jdbc.repository.movement;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.account.AccountBalanceEntity;
import com.dfedorino.cashy.domain.model.category.CategoryBalanceEntity;
import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.model.direction.DirectionTypes;
import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import com.dfedorino.cashy.domain.model.movement.BalanceMovementEntity;
import com.dfedorino.cashy.domain.model.operation.OperationEntity;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.account.AccountBalanceRepository;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceRepository;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.movement.BalanceMovementRepository;
import com.dfedorino.cashy.domain.repository.operation.OperationRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcBalanceMovementRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private OperationRepository operationRepository;
    private AccountBalanceRepository accountBalanceRepository;
    private CategoryBalanceRepository categoryBalanceRepository;
    private BalanceMovementRepository balanceMovementRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        categoryRepository = ctx.getBean(CategoryRepository.class);
        operationRepository = ctx.getBean(OperationRepository.class);
        accountBalanceRepository = ctx.getBean(AccountBalanceRepository.class);
        categoryBalanceRepository = ctx.getBean(CategoryBalanceRepository.class);
        balanceMovementRepository = ctx.getBean(BalanceMovementRepository.class);
    }

    @Test
    void create_income_balance_movement() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN,
                TestConstants.PASSWORD_HASH
        )));

        var accountBalance = tx(() -> accountBalanceRepository.create(new AccountBalanceEntity(
                user.getId(),
                BigDecimal.ZERO
        )));

        var incomeCategory = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        var incomeCategoryBalance = tx(
                () -> categoryBalanceRepository.create(new CategoryBalanceEntity(
                        user.getId(),
                        incomeCategory.getId(),
                        BigDecimal.ZERO
                )));

        var incomeOperation = tx(() -> operationRepository.createOperation(new OperationEntity(
                user.getId(),
                incomeCategory.getId(),
                BigDecimal.TEN
        )));

        var incomeBalanceMovement = tx(
                () -> balanceMovementRepository.createMovement(new BalanceMovementEntity(
                        user.getId(),
                        incomeOperation.getId(),
                        accountBalance.getId(),
                        incomeCategoryBalance.getId(),
                        DirectionTypes.CREDIT.getId(),
                        BigDecimal.TEN
                )));

        assertThat(incomeBalanceMovement.getId()).isOne();
        assertThat(incomeBalanceMovement.getUserId()).isEqualTo(user.getId());
        assertThat(incomeBalanceMovement.getOperationId()).isEqualTo(incomeOperation.getId());
        assertThat(incomeBalanceMovement.getAccountBalanceId()).isEqualTo(accountBalance.getId());
        assertThat(incomeBalanceMovement.getCategoryBalanceId()).isEqualTo(incomeCategoryBalance.getId());
        assertThat(incomeBalanceMovement.getDirectionTypeId()).isEqualTo(DirectionTypes.CREDIT.getId());
        assertThat(incomeBalanceMovement.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(incomeBalanceMovement.getCreatedAt()).isNotNull();
    }
}