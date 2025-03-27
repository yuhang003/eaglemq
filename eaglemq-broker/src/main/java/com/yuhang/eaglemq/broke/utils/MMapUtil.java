package com.yuhang.eaglemq.broke.utils;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Scanner;

public class MMapUtil {

    private MappedByteBuffer mappedByteBuffer;

    public void loadFileInMMap(String filePath, int startOffset, int mappedSize) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("filePath is :[" + filePath + "] not found");
        }

        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
             FileChannel fileChannel = accessFile.getChannel()) {
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, startOffset, mappedSize);
        }
    }

    public byte[] readContent(int readOffSet, int size) {
        mappedByteBuffer.position(readOffSet);

        byte[] content = new byte[size];
        for (int i = 0; i < size; i++) {
            content[i] = mappedByteBuffer.get(readOffSet + i);
        }
        return content;
    }

    public void writeContent(byte[] content) {
        writeContent(content, false);
    }

    public void writeContent(byte[] content, boolean force) {
        mappedByteBuffer.put(content);
        if (force) {
            mappedByteBuffer.force();
        }
    }

    public void clean() {
        if (mappedByteBuffer == null || !mappedByteBuffer.isDirect() || mappedByteBuffer.capacity() == 0)
            return;
        invoke(invoke(viewed(mappedByteBuffer), "cleaner"), "clean");
    }

    private Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    private Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null)
            return buffer;
        else
            return viewed(viewedBuffer);
    }

    public static void main(String[] args) throws Exception {
//        MMapUtil mMapUtil = new MMapUtil();
//        //映射1KB
//        mMapUtil.loadFileInMMap("D:\\Code\\JAVA\\mianshi_2025\\eaglemq\\borker\\store\\order_cancel_topic\\00000000", 0, 1024);

//        Scanner scanner = new Scanner(System.in);
//        System.out.println("请输入第一个字符：");
//        String s1 = scanner.nextLine();
//        mMapUtil.clean();

        try (FileInputStream fileInputStream = new FileInputStream("D:\\Code\\JAVA\\mianshi_2025\\eaglemq\\borker\\store\\order_cancel_topic\\00000000")) {
            byte[] buffer = new byte[1024]; // 每次读取1KB的数据
            int bytesRead;

            System.out.println("文件内容如下：");
            // 循环读取文件数据，直到文件末尾
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                // 将读取到的字节数组转换为字符串并输出
                System.out.print(new String(buffer, 0, bytesRead));
                return;
            }
        } catch (IOException e) {
            System.err.println("读取文件时发生错误：" + e.getMessage());
        }
    }
}
