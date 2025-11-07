package com.dfedorino.cashy.domain.model.movement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectionTypeDictionary {

    private Long id;
    private String code;
    private String name;
}
