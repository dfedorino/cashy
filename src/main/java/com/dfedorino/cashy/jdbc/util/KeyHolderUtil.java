package com.dfedorino.cashy.jdbc.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.springframework.jdbc.support.KeyHolder;

@UtilityClass
public class KeyHolderUtil {

    public static final String ID = "ID";
    public static final String CREATED_AT = "CREATED_AT";
    public static final String UPDATED_AT = "UPDATED_AT";


    public Long getId(KeyHolder keyHolder) {
        return (Long) Objects.requireNonNull(keyHolder.getKeys()).get(ID);
    }

    public LocalDateTime getCreatedAt(KeyHolder keyHolder) {
        return ((Timestamp) Objects.requireNonNull(keyHolder.getKeys()).get(CREATED_AT))
                .toLocalDateTime();
    }

    public LocalDateTime getUpdatedAt(KeyHolder keyHolder) {
        return ((Timestamp) Objects.requireNonNull(keyHolder.getKeys()).get(UPDATED_AT))
                .toLocalDateTime();
    }
}
