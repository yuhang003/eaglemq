package com.yuhang.eaglemq.broke.config;

import com.alibaba.fastjson2.JSON;
import com.yuhang.eaglemq.broke.cache.CommonCache;
import com.yuhang.eaglemq.broke.model.EagleMqTopicModel;
import com.yuhang.eaglemq.broke.utils.FileContentUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.yuhang.eaglemq.broke.constants.BrokerConstants.DEFAULT_REFRESH_MQ_TOPIC_TIME_STEP;
import static com.yuhang.eaglemq.broke.constants.BrokerConstants.EAGLE_MQ_CONFIG_FILE_PATH;

/**
 * 负责将mq的主题配置信息加载到内存中
 */
@Slf4j
public class EagleMqTopicLoader {

    private final String filePath;

    public EagleMqTopicLoader() {
        GlobalProperties globalProperties = CommonCache.getGlobalProperties();
        String basePath = globalProperties.getEagleMqHome();

        filePath = basePath + EAGLE_MQ_CONFIG_FILE_PATH;
    }

    public void loadProperties() {
        String fileContent = FileContentUtil.readFromFile(filePath);
        List<EagleMqTopicModel> eagleMqTopicModelList = JSON.parseArray(fileContent, EagleMqTopicModel.class);
        CommonCache.setEagleMqTopicModelList(eagleMqTopicModelList);
    }

    /**
     * 开启一个刷新内存到磁盘的任务
     */
    public void startRefreshEagleMqTopicInfoTask() {
        //异步线程
        //每3秒将内存中的配置刷新到磁盘里面
        CommonThreadPoolConfig.refreshEagleMqTopicExecutor.execute(() -> {
            do {
                try {
                    TimeUnit.SECONDS.sleep(DEFAULT_REFRESH_MQ_TOPIC_TIME_STEP);
                    List<EagleMqTopicModel> eagleMqTopicModelList = CommonCache.getEagleMqTopicModelList();
                    FileContentUtil.overWriteToFile(filePath, JSON.toJSONString(eagleMqTopicModelList));
                    log.info("refresh disk");

                    // 判断主线程是否关闭了线程池，如果关闭了，该任务也就应该相应取消，以达到正常关闭线程池的目的
                    if (CommonThreadPoolConfig.refreshEagleMqTopicExecutor.isShutdown()) {
                        log.info("refresh disk is shutdown");
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("refresh disk error", e);
                }
            } while (!Thread.currentThread().isInterrupted());
        });
    }
}
