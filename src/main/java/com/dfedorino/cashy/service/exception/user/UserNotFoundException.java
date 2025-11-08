package com.dfedorino.cashy.service.exception.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String login) {
        super("User not found by login: " + login);
    }
}
