package com.dfedorino.cashy.jdbc.repository.user;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcUserRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
    }

    @Test
    void user_created_and_found() {
        UserEntity user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        assertThat(user.getId()).isNotNull();
        assertThat(user.getLogin()).isEqualTo(TestConstants.LOGIN);
        assertThat(user.getPasswordHash()).isEqualTo(TestConstants.PASSWORD_HASH);
        assertThat(user.getCreatedAt()).isNotNull();

        Optional<UserEntity> found = tx(() -> userRepository.findByLogin(TestConstants.LOGIN));
        assertThat(found).contains(user);

        Optional<UserEntity> lost = tx(() -> userRepository.findByLogin("error"));
        assertThat(lost).isEmpty();
    }
}