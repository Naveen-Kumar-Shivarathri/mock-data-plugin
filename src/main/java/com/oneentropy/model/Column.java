package com.oneentropy.model;

import com.oneentropy.common.Strategy;
import com.oneentropy.util.CommonUtil;
import lombok.*;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Column {

    private static final String BOUNDARY_REGX = "(.*)\\((\\d*)\\)";
    private static final Pattern PATTERN = Pattern.compile(BOUNDARY_REGX);

    private static final String STRING_ESCAPE_CHAR = "\"";

    private static final Log logger = new SystemStreamLog();

    private String columnName;
    private String dataType;
    private String key;
    private Strategy strategy;
    private String seededValue;
    private boolean isNullable;

    public boolean containsKey() {
        return CommonUtil.hasContent(key);
    }

    public boolean containsForeignKey() {
        return this.key.equalsIgnoreCase("mul");
    }

    public boolean containsBoundaries() {
        return this.dataType.matches(BOUNDARY_REGX);
    }

    public String getEquivalentValue(int iteration) {
        logger.info("generating equivalent value of column:" + this.columnName);
        String trimmedDataType = getTrimmedDataType();
        if (!CommonUtil.hasContent(trimmedDataType))
            return "NULL";
        String value = null;
        switch (trimmedDataType) {
            case "bit":
                value = handleBit(iteration);
                break;
            case "varchar":
            case "text":
            case "char":
                value = handleCharTypeData(iteration);
                value = transformToString(value);
                break;
            case "int":
            case "tinyint":
            case "bigint":
            case "decimal":
                value = handleInt(iteration);
                break;
            case "timestamp":
            case "datetime":
                value = handleTimestamp(iteration);
                value = transformToString(value);
                break;
            case "time":
                value = handleTime(iteration);
                value = transformToString(value);
                break;
            case "json":
                value = handleJson(iteration);
                value = transformToString(value);
                break;

        }
        if (!isNullable)
            return value;

        return "NULL";
    }

    private String transformToString(String value) {
        return STRING_ESCAPE_CHAR + value + STRING_ESCAPE_CHAR;
    }

    private String handleJson(int iteration){
        return "{\\\""+this.columnName+"\\\":"+iteration+"}";
    }
    private String handleTime(int iteration) {

        if (strategy != null && strategy.equals(Strategy.CONSTANT))
            return adjustOnBoundary(seededValue, -1);

        return formatTime("hh:mm:ss");
    }

    private String handleTimestamp(int iteration) {

        if (strategy != null && strategy.equals(Strategy.CONSTANT))
            return adjustOnBoundary(seededValue, -1);

        return formatTime("yyyy-MM-dd hh:mm:ss");
    }

    private String formatTime(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date currentDate = new Date();
        return sdf.format(currentDate);
    }

    private String handleInt(int iteration) {
        logger.info("Handling int type data");
        int boundary = getBoundary();
        logger.info("Boundary:" + boundary);
        if (strategy != null && strategy.equals(Strategy.CONSTANT))
            return adjustOnBoundary(seededValue, boundary);

        int power = determinePower(iteration);
        if(CommonUtil.hasContent(seededValue)){
            return adjustOnBoundary(seededValue+iteration,boundary);
        }
        logger.info("Power:" + power);
        if (boundary == power)
            return adjustOnBoundary(iteration + "", boundary);
        if (boundary < 1) {
            return adjustOnBoundary(iteration + "", boundary);
        }
        int maxValue = (int) Math.pow(2, boundary);
        int remainder = iteration % maxValue;
        return adjustOnBoundary(remainder + "", boundary);
    }

    private String handleCharTypeData(int iteration) {
        int boundary = getBoundary();

        if (strategy != null && strategy.equals(Strategy.CONSTANT))
            return adjustOnBoundary(seededValue, boundary);

        StringBuilder sb = new StringBuilder();
        if (seededValue != null) {
            return adjustOnBoundary(seededValue + iteration, boundary);
        }
        sb.append(columnName);
        sb.append(iteration);
        return adjustOnBoundary(sb.toString(), boundary);
    }

    private String adjustOnBoundary(String value, int boundary) {
        if (boundary < 1)
            return value;
        if (value.length() > boundary) {
            int lastIndex = value.length() - boundary;
            return value.substring(0, value.length() - lastIndex);
        }
        return value;
    }

    private String handleBit(int iteration) {
        int power = determinePower(iteration);
        int boundary = getBoundary();
        if (power <= boundary) {
            return "b'" + Integer.toBinaryString(iteration) + "'";
        }
        int maxValue = (int) Math.pow(2, boundary);
        int remainder = iteration % maxValue;
        return "b'" + Integer.toBinaryString(remainder) + "'";
    }

    private String getTrimmedDataType() {
        if (this.dataType.matches(BOUNDARY_REGX)) {
            Matcher matcher = PATTERN.matcher(this.dataType);
            if (matcher.matches()) {
                String dt = matcher.group(1);
                return dt;
            }


        }

        return this.dataType;
    }


    private int determinePower(int iteration) {
        int count = 0;
        while (iteration != 0) {
            iteration = iteration >> 1;
            count++;
        }
        return count;
    }

    public int getBoundary() {
        if (this.dataType.matches(BOUNDARY_REGX)) {
            Matcher matcher = PATTERN.matcher(this.dataType);
            if (matcher.matches()) {
                String stringInt = matcher.group(2);
                if (stringInt != null)
                    return Integer.parseInt(stringInt);
            }

        }

        return -1;
    }

}
