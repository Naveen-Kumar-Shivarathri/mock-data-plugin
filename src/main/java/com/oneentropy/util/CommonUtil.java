package com.oneentropy.util;

public class CommonUtil {

    public static boolean hasContent(Object value){
        if(value==null)
            return false;
        if(value.toString().equals("")||value.toString().matches("\\s*"))
            return false;
        return true;
    }
}
