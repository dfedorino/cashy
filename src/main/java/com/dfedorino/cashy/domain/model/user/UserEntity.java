package com.dfedorino.cashy.domain.model.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    private Long id;
    private String login;
    private String passwordHash;
    private LocalDateTime createdAt;

    public UserEntity(String login, String passwordHash) {
        this(null, login, passwordHash, null);
    }
}
