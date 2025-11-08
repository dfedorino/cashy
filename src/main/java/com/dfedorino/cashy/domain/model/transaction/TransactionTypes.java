package com.dfedorino.cashy.domain.model.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionTypes {
    INCOME(1L), EXPENSE(2L);
    private final long id;
}
