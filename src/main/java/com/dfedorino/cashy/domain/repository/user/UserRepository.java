package com.dfedorino.cashy.domain.repository.user;

import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.exception.RepositoryException;
import java.util.Optional;

/**
 * Repository interface for managing {@link UserEntity} persistence.
 */
public interface UserRepository {

    /**
     * Persists a new user.
     *
     * @param user the {@link UserEntity} to create
     * @return the created {@link UserEntity} with generated ID
     * @throws RepositoryException if any error occurs
     */
    UserEntity createUser(UserEntity user);

    /**
     * Retrieves a user by their unique login.
     *
     * @param login the login of the user
     * @return an {@link Optional} containing the {@link UserEntity} if found, or empty if not found
     * @throws RepositoryException if any error occurs
     */
    Optional<UserEntity> findByLogin(String login);
}

