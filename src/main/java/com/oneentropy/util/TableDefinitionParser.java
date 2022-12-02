package com.oneentropy.util;


import com.oneentropy.common.Strategy;
import com.oneentropy.model.Column;
import com.oneentropy.model.TableMetaData;
import com.oneentropy.model.TableStruct;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TableDefinitionParser {

    public static String DELIMITER = ",";

    private static Log logger = new SystemStreamLog();

    public static List<TableStruct> parse(List<TableMetaData> tableMetaDataList) {
        List<TableStruct> tableStructList = new LinkedList<>();
        for (TableMetaData tableMetaData : tableMetaDataList) {
            TableStruct tableStruct = parse(tableMetaData.getDatabase(), tableMetaData.getTableName(), tableMetaData.getTableDefPath());
            if (tableStruct != null)
                tableStructList.add(tableStruct);
        }
        return tableStructList;
    }

    public static TableStruct parse(String database, String tableName, String filePath) {
        try {
            String rawTableInfo = FileUtil.readContentsFrom(filePath);
            if (!CommonUtil.hasContent(rawTableInfo))
                return null;
            String[] columnDefs = rawTableInfo.split("\\n", -1);
            logger.info("Column Definitions:" + (columnDefs.length - 1) + "");
            if (columnDefs.length < 2)
                return null;
            return iterateTableDef(database, tableName, columnDefs);
        } catch (IOException e) {
            return null;
        }

    }

    private static TableStruct iterateTableDef(String database, String tableName, String[] tableDefs) {
        boolean isHeader = true;
        List<Column> columnList = new LinkedList<>();
        for (String tableDef : tableDefs) {
            if (isHeader) {
                isHeader = false;
                continue;
            }
            String[] tokens = tableDef.split(DELIMITER, -1);
            if (tokens != null && tokens.length == 6) {
                String columnName = tokens[0];
                String columnDataType = tokens[1];
                boolean isNullable = isNullable(tokens[2]);
                String key = tokens[3];
                String seededValue = tokens[4];
                String strategyValue = tokens[5];
                Strategy strategy = null;
                if (CommonUtil.hasContent(strategyValue)) {
                    strategy = Strategy.valueOf(strategyValue.trim());
                }
                columnList.add(Column.builder().columnName(columnName).dataType(columnDataType).isNullable(isNullable).key(key).seededValue(seededValue).strategy(strategy).build());
            }
        }
        return TableStruct.builder().database(database).tableName(tableName).columnList(columnList).build();
    }

    private static boolean isNullable(String nullable) {
        return nullable.equalsIgnoreCase("yes");
    }

}
