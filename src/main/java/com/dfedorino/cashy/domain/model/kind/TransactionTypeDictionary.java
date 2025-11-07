package com.dfedorino.cashy.domain.model.kind;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTypeDictionary {

    private Long id;
    private String code;
    private String name;
}
