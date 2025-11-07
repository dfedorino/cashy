package com.dfedorino.cashy.jdbc.repository.user;

import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.util.KeyHolderUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {

    public static final String LOGIN = "login";
    public static final String PASSWORD_HASH = "passwordHash";

    public static final String INSERT_INTO_USER =
            "INSERT INTO \"user\"(login, password_hash) "
                    + "VALUES (:" + LOGIN + ", :" + PASSWORD_HASH + ")";

    public static final String SELECT_BY_LOGIN = "SELECT * FROM \"user\" "
            + "WHERE login = :" + LOGIN;

    private final JdbcClient jdbcClient;

    @Override
    public UserEntity createUser(UserEntity user) {
        var keyHolder = new GeneratedKeyHolder();

        try {
            jdbcClient.sql(INSERT_INTO_USER)
                    .param(LOGIN, user.getLogin())
                    .param(PASSWORD_HASH, user.getPasswordHash())
                    .update(keyHolder);
        } catch (Exception e) {
            log.error(">> Failed to create user with login: {}", user.getLogin());
            log.error(">> ", e);
            throw new RepositoryException(e);
        }

        user.setId(KeyHolderUtil.getId(keyHolder));
        user.setCreatedAt(KeyHolderUtil.getCreatedAt(keyHolder));

        return user;
    }

    @Override
    public Optional<UserEntity> findByLogin(String login) {
        try {
            return jdbcClient.sql(SELECT_BY_LOGIN)
                    .param(LOGIN, login)
                    .query(UserEntity.class)
                    .optional();
        } catch (Exception e) {
            log.error(">> Failed to find user with login: {}", login);
            log.error(">> ", e);
            throw new RepositoryException(e);
        }
    }
}
