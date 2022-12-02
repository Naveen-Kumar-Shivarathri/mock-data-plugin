package com.oneentropy.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TableConfig {

    private List<String> exclusion;
    private String delimiter;
    private Integer records;

}
