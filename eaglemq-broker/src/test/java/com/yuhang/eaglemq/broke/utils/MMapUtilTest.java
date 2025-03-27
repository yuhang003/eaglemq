package com.yuhang.eaglemq.broke.utils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;


public class MMapUtilTest {

    private static String filePath = "D:\\Code\\JAVA\\mianshi_2025\\eaglemq\\borker\\store\\order_cancel_topic\\00000000";

    private static MMapUtil mMapUtil;

    @BeforeClass
    public static void setUp() throws IOException {
        mMapUtil = new MMapUtil();
        mMapUtil.loadFileInMMap(filePath, 0, 10 * 1024);
    }

    @Test
    public void testWriteAndReadFile() {
        String content = "this is a test content";
        mMapUtil.writeContent(content.getBytes());

        byte[] readContent = mMapUtil.readContent(0, content.length() + 100);
        System.out.println(new String(readContent));
    }
}
