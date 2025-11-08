package com.dfedorino.cashy.jdbc.repository.operation;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.model.kind.TransactionTypes;
import com.dfedorino.cashy.domain.model.operation.OperationEntity;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.operation.OperationRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcOperationRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private OperationRepository operationRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        categoryRepository = ctx.getBean(CategoryRepository.class);
        operationRepository = ctx.getBean(OperationRepository.class);
    }

    @Test
    void create_income_operation() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN,
                TestConstants.PASSWORD_HASH
        )));

        var incomeCategory = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        var incomeOperation = tx(() -> operationRepository.createOperation(new OperationEntity(
                user.getId(),
                incomeCategory.getId(),
                BigDecimal.TEN
        )));

        assertThat(incomeOperation.getId()).isOne();
        assertThat(incomeOperation.getUserId()).isEqualTo(user.getId());
        assertThat(incomeOperation.getCategoryId()).isEqualTo(incomeCategory.getId());
        assertThat(incomeOperation.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(incomeOperation.getCreatedAt()).isNotNull();
    }
}