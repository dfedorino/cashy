package com.dfedorino.cashy.domain.model.transaction;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionTypes {
    INCOME(1L), EXPENSE(2L);
    private final long id;

    public static TransactionTypes of(Long id) {
        return Arrays.stream(values())
                .filter(v -> v.getId() == id)
                .findAny().orElseThrow();
    }
}
