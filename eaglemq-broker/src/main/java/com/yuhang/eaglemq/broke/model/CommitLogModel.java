package com.yuhang.eaglemq.broke.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

import static com.yuhang.eaglemq.broke.constants.BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE;

@Data
public class CommitLogModel {

    private String fileName;
    private AtomicInteger offset;
    private Integer offsetLimit = COMMIT_LOG_DEFAULT_MMAP_SIZE;

    public int countDiff() {
        return offsetLimit - offset.get();
    }
}
