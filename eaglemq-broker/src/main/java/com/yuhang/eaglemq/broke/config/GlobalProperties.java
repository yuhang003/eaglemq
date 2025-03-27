package com.yuhang.eaglemq.broke.config;

import lombok.Data;

@Data
public class GlobalProperties {

    /**
     * 读取环境变量中配置的mq存储地址
     */
    private String eagleMqHome;
}
