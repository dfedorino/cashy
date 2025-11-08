package com.dfedorino.cashy.domain.model.direction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DirectionTypes {
    CREDIT(1L), DEBIT(2L);
    private final long id;
}
