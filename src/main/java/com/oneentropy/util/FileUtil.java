package com.oneentropy.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtil {

    public static String readContentsFrom(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }

    public static void dumpDataTo(String data, String filePath) throws IOException{
        if(!new File(filePath).exists()){
            new File(filePath).getParentFile().mkdir();
        }
        Files.writeString(Paths.get(filePath), data, StandardOpenOption.CREATE,StandardOpenOption.WRITE);
    }

}
