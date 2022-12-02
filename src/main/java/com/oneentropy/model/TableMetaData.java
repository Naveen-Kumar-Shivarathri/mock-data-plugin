package com.oneentropy.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TableMetaData {

    private String database;
    private String tableName;
    private String tableDefPath;

}
