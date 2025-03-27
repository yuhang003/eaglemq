package com.yuhang.eaglemq.broke.core;

import com.yuhang.eaglemq.broke.cache.CommonCache;
import com.yuhang.eaglemq.broke.model.CommitLogFilePath;
import com.yuhang.eaglemq.broke.model.CommitLogMessageModel;
import com.yuhang.eaglemq.broke.model.CommitLogModel;
import com.yuhang.eaglemq.broke.model.EagleMqTopicModel;
import com.yuhang.eaglemq.broke.utils.CommitLogFileNameUtil;
import com.yuhang.eaglemq.broke.utils.PutMessageLock;
import com.yuhang.eaglemq.broke.utils.UnfairReentrantLock;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

import static com.yuhang.eaglemq.broke.constants.BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE;

/**
 * 最基础的mmap对象模型
 */
@Slf4j
public class MMapFileModel {

    private MappedByteBuffer mappedByteBuffer;
    private final String topicName;
    private final PutMessageLock putMessageLock;

    public MMapFileModel(String topicName) {
        this.topicName = topicName;
        putMessageLock = new UnfairReentrantLock();
    }

    /**
     * 指定offset做文件的映射
     *
     * @param startOffset 开始映射的offset
     * @param mappedSize  映射的体积 (byte)
     */
    public void loadFileInMMap(int startOffset, int mappedSize) throws IOException {
        String filePath = getLatestCommitLogFile();
        doMMap(filePath, startOffset, mappedSize);
    }


    /**
     * 执行mmap步骤
     *
     * @param filePath
     * @param startOffset
     * @param mappedSize
     * @throws IOException
     */
    private void doMMap(String filePath, int startOffset, int mappedSize) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("filePath is :[" + filePath + "] not found");
        }

        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
             FileChannel fileChannel = accessFile.getChannel()) {
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, startOffset, mappedSize);
        }
    }

    private String getLatestCommitLogFile() {
        CommitLogModel commitLogModel = getCommitLogModel(topicName);
        int diff = commitLogModel.countDiff();

        String filePath = null;
        if (diff <= 0) {
            CommitLogFilePath newCommitLogFile = createNewCommitLogFile(topicName, commitLogModel);
            filePath = newCommitLogFile.getFilePath();
        } else {
            filePath = CommitLogFileNameUtil.buildCommitLogFilePath(topicName, commitLogModel.getFileName());
        }
        return filePath;
    }

    /**
     * 支持从文件的指定offset开始读取内容
     *
     * @param readOffSet
     * @param size
     * @return
     */
    public byte[] readContent(int readOffSet, int size) {
        mappedByteBuffer.position(readOffSet);

        byte[] content = new byte[size];
        for (int i = 0; i < size; i++) {
            content[i] = mappedByteBuffer.get(readOffSet + i);
        }
        return content;
    }

    /**
     * 更高性能的一种写入api
     *
     * @param commitLogMessageModel
     */
    public void writeContent(CommitLogMessageModel commitLogMessageModel) throws IOException {
        writeContent(commitLogMessageModel, false);
    }

    /**
     * 写入数据到磁盘当中
     *
     * @param commitLogMessageModel
     */
    public void writeContent(CommitLogMessageModel commitLogMessageModel, boolean force) throws IOException {
        CommitLogModel commitLogModel = getCommitLogModel(topicName);

        // 写入加锁
        putMessageLock.lock();

        // 检查当前文件是否还有足够的空间写入这条消息
        checkCommitLogHasEnableSpace(commitLogMessageModel, commitLogModel);

        //  获取需要写入的消息内容
        byte[] writeContent = commitLogMessageModel.convertToBytes();
        mappedByteBuffer.put(writeContent);
        // 写入消息后，将offset增加
        commitLogModel.getOffset().addAndGet(writeContent.length);
        if (force) {
            mappedByteBuffer.force();
        }

        // 解锁
        putMessageLock.unlock();
    }

    private CommitLogFilePath createNewCommitLogFile(String topic, CommitLogModel commitLogModel) {
        String newFileName = CommitLogFileNameUtil.incrCommitLogFileName(commitLogModel.getFileName());
        String newFilePath = CommitLogFileNameUtil.buildCommitLogFilePath(topic, newFileName);
        File newCommitLogFile = new File(newFilePath);

        try {
            newCommitLogFile.createNewFile();
            log.info("created new commitLog file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new CommitLogFilePath(newFileName, newFilePath);
    }

    /**
     * 判断当前文件是否还有空间可以写下commitLogMessageModel中的消息
     * @param commitLogMessageModel
     * @param commitLogModel
     * @return
     */
    private void checkCommitLogHasEnableSpace(CommitLogMessageModel commitLogMessageModel, CommitLogModel commitLogModel) throws IOException {
        int writeAbleOffsetNum = commitLogModel.countDiff();
        // 如果不够写入
        if (writeAbleOffsetNum < commitLogMessageModel.getSize()) {
            // 创建新的文件
            CommitLogFilePath newCommitLogFilePath = createNewCommitLogFile(topicName, commitLogModel);
            // 将topic的文件名设置为新的文件，并重置Offset
            commitLogModel.setFileName(newCommitLogFilePath.getFileName());
            commitLogModel.setOffset(new AtomicInteger(0));
            commitLogModel.setOffsetLimit(COMMIT_LOG_DEFAULT_MMAP_SIZE);

            // 老文件MMap映射解绑
            clean();
            // 新文件做MMap映射
            doMMap(newCommitLogFilePath.getFilePath(), 0, COMMIT_LOG_DEFAULT_MMAP_SIZE);
        }
    }

    /**
     * 释放mmap内存占用
     */
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

    /**
     * 获取当前topic下的commitLog信息
     * @return
     */
    private CommitLogModel getCommitLogModel(String topic) {
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            throw new RuntimeException("eagleMqTopicModel is null");
        }

        CommitLogModel commitLogModel = eagleMqTopicModel.getCommitLogModel();
        if (commitLogModel == null) {
            throw new RuntimeException("commitLogModel is null");
        }
        return commitLogModel;
    }
}
