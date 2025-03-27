package com.yuhang.eaglemq.broke.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class CommitLogModel {

    private String fileName;
    private AtomicLong offset;
    private Long offsetLimit;

    public Long countDiff() {
        return offsetLimit - offset.get();
    }
}
