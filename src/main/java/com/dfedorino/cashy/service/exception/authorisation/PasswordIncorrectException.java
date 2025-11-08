package com.dfedorino.cashy.service.exception.authorisation;

public class PasswordIncorrectException extends RuntimeException {

    public PasswordIncorrectException() {
        super("Password incorrect!");
    }
}
