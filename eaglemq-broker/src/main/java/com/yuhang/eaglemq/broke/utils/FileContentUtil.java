package com.yuhang.eaglemq.broke.utils;

import java.io.*;

public class FileContentUtil {

    public static String readFromFile(String filePath) {

        try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {
            StringBuilder stringBuffer = new StringBuilder();
            while (in.ready()) {
                stringBuffer.append(in.readLine());
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void overWriteToFile(String filePath, String content) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
