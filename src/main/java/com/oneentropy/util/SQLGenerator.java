package com.oneentropy.util;

import com.oneentropy.model.Column;
import com.oneentropy.model.TableStruct;

import java.util.List;

public class SQLGenerator {

    public static String generateMultiInserts(TableStruct tableStruct, int records){
        StringBuilder sb = new StringBuilder();
        for(int iteration=0;iteration<records;iteration++){
            sb.append(generateInsertSql(tableStruct,iteration));
            sb.append("\r\n");
        }
        return sb.toString();
    }


    public static String generateInsertSql(TableStruct tableStruct, int iteration){
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tableStruct.getDatabase());
        sb.append(".");
        sb.append(tableStruct.getTableName());
        sb.append(generateColumnDescriptors(tableStruct.getColumnList()));
        sb.append(" values");
        sb.append(generateColumnMockValues(tableStruct.getColumnList(),iteration));
        sb.append(";");
        return sb.toString();
    }

    private static String generateColumnDescriptors(List<Column> columnList){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(Column column: columnList){
            sb.append(column.getColumnName());
            sb.append(",");
        }
        sb.delete(sb.length()-1,sb.length());
        sb.append(")");
        return sb.toString();
    }

    private static String generateColumnMockValues(List<Column> columnList, int iteration){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(Column column: columnList){
            sb.append(column.getEquivalentValue(iteration));
            sb.append(",");
        }
        sb.delete(sb.length()-1,sb.length());
        sb.append(")");
        return sb.toString();
    }


}
