package com.yuhang.eaglemq.broke.model;

import lombok.Data;

import java.util.List;

@Data
public class EagleMqTopicModel {

    private String topic;
    private CommitLogModel commitLogModel;
    private List<QueueModel> queueList;
    private Long createAt;
    private Long updateAt;
}
