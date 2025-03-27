package com.yuhang.eaglemq.broke.model;

import lombok.Data;

@Data
public class QueueModel {

    private Integer id;
    private Long currentOffset;
    private Long minOffset;
    private Long maxOffset;
}
