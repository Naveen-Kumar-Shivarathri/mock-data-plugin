package com.oneentropy.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TableStruct {

    private String database;
    private String tableName;
    private List<Column> columnList;


}
