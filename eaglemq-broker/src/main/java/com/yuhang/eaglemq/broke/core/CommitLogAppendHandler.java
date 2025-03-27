package com.yuhang.eaglemq.broke.core;

import com.yuhang.eaglemq.broke.model.CommitLogMessageModel;

import java.io.IOException;

import static com.yuhang.eaglemq.broke.constants.BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE;


public class CommitLogAppendHandler {

    private final MMapFileModelManager mMapFileModelManager = new MMapFileModelManager();

    /**
     * 构建topic -> MMap对象的Map
     */
    public void prepareMMapLoading(String topic) throws IOException {
        MMapFileModel mapFileModel = new MMapFileModel(topic);
        mapFileModel.loadFileInMMap(0, COMMIT_LOG_DEFAULT_MMAP_SIZE);
        mMapFileModelManager.put(topic, mapFileModel);
    }

    /**
     * 追加写入消息
     * @param topic
     * @param content
     * @throws IOException
     */
    public void appendMsg(String topic, byte[] content) throws IOException {
        MMapFileModel mapFileModel = mMapFileModelManager.get(topic);
        if (mapFileModel == null) {
            throw new RuntimeException("topicName: [" + topic + "] is invalid!");
        }

        CommitLogMessageModel commitLogMessageModel = CommitLogMessageModel.builder()
                .size(content.length + 4)
                .content(content)
                .build();
        mapFileModel.writeContent(commitLogMessageModel);
    }

    /**
     * 读取消息
     * @param topic
     * @return
     * @throws IOException
     */
    public String readMsg(String topic) throws IOException {
        MMapFileModel mapFileModel = mMapFileModelManager.get(topic);
        if (mapFileModel == null) {
            throw new RuntimeException("topicName: [" + topic + "] is invalid!");
        }
        byte[] content = mapFileModel.readContent(0, 1000);
        return new String(content);
    }
}
