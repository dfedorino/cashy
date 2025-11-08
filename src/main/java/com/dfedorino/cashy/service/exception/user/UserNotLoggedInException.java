package com.dfedorino.cashy.service.exception.user;

public class UserNotLoggedInException extends RuntimeException {

    public UserNotLoggedInException() {
        super("User not logged in!");
    }
}
