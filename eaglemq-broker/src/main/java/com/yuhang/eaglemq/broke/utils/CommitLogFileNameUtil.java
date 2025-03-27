package com.yuhang.eaglemq.broke.utils;

import com.yuhang.eaglemq.broke.cache.CommonCache;
import com.yuhang.eaglemq.broke.constants.BrokerConstants;

public class CommitLogFileNameUtil {

    /**
     * 构建第一份commitLog文件名称
     * @return
     */
    public static String buildFirstCommitLogName() {
        return "00000000";
    }

    /**
     * 构建新的commitLog文件路径
     *
     * @param topicName
     * @param commitLogFileName
     * @return
     */
    public static String buildCommitLogFilePath(String topicName, String commitLogFileName) {
        return CommonCache.getGlobalProperties().getEagleMqHome()
                + BrokerConstants.BASE_STORE_PATH
                + topicName
                + "/"
                + commitLogFileName;
    }

    /**
     * 根据老的commitLog文件名生成新的commitLog文件名
     * @param oldFileName
     * @return
     */
    public static String incrCommitLogFileName(String oldFileName) {
        if (oldFileName.length() != 8) {
            throw new IllegalArgumentException("fileName must has 8 chars");
        }

        Integer fileIndex = Integer.valueOf(oldFileName);
        fileIndex++;
        String newFileName = String.valueOf(fileIndex);
        int needFullLen = 8 - newFileName.length();
        if (needFullLen < 0) {
            throw new RuntimeException("unKnow fileName error");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < needFullLen; i++) {
            builder.append("0");
        }
        builder.append(newFileName);
        return builder.toString();
    }
}
