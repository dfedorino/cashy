package com.dfedorino.cashy.service;

import com.dfedorino.cashy.domain.model.account.AccountBalanceEntity;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.account.AccountBalanceRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.service.dto.UserDto;
import com.dfedorino.cashy.service.exception.authorisation.PasswordIncorrectException;
import com.dfedorino.cashy.service.exception.user.UserNotFoundException;
import com.dfedorino.cashy.service.exception.user.UserNotLoggedInException;
import com.dfedorino.cashy.util.AuthorisationUtil;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final TransactionTemplate tx;

    public void registerUser(String login, String password) {
        UserDto user = new UserDto(safeTx($ -> {
            UserEntity createdUser = userRepository.createUser(new UserEntity(
                    login,
                    AuthorisationUtil.getEncodedPassword(password)
            ));
            accountBalanceRepository.create(new AccountBalanceEntity(
                    createdUser.getId(),
                    BigDecimal.ZERO
            ));
            return createdUser;
        }).getLogin());

        AuthorisationUtil.login(user);
    }

    public UserDto loginUser(String login, String password) {
        UserEntity found = safeTx($ -> userRepository.findByLogin(login))
                .orElseThrow(() -> new UserNotFoundException(login));

        if (!AuthorisationUtil.isValidPassword(password, found.getPasswordHash())) {
            throw new PasswordIncorrectException();
        }

        var userDto = new UserDto(found.getLogin());
        AuthorisationUtil.login(userDto);
        return userDto;
    }

    public void logout() {
        if (AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }

        AuthorisationUtil.logout();
    }

    private <T> T safeTx(TransactionCallback<T> callback) {
        return Objects.requireNonNull(tx.execute(callback));
    }

}
