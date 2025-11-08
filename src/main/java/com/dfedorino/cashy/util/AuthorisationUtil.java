package com.dfedorino.cashy.util;

import com.dfedorino.cashy.service.dto.UserDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthorisationUtil {
    private static final ThreadLocal<UserDto> CURRENT_USER = new ThreadLocal<>();
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public static void login(UserDto user) {
        CURRENT_USER.set(user);
    }

    public static void logout() {
        CURRENT_USER.remove();
    }

    public static UserDto getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static String getEncodedPassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    public static boolean isValidPassword(String password, String passwordHash) {
        return PASSWORD_ENCODER.matches(password, passwordHash);
    }
}
