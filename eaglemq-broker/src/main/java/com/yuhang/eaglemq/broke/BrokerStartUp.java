package com.yuhang.eaglemq.broke;

import com.yuhang.eaglemq.broke.cache.CommonCache;
import com.yuhang.eaglemq.broke.config.CommonThreadPoolConfig;
import com.yuhang.eaglemq.broke.config.EagleMqTopicLoader;
import com.yuhang.eaglemq.broke.config.GlobalPropertiesLoader;
import com.yuhang.eaglemq.broke.core.CommitLogAppendHandler;
import com.yuhang.eaglemq.broke.model.EagleMqTopicModel;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Broker 启动类
 */
public class BrokerStartUp {

    private GlobalPropertiesLoader globalPropertiesLoader;
    private EagleMqTopicLoader eagleMqTopicLoader;
    private CommitLogAppendHandler commitLogAppendHandler;

    /**
     * 初始化配置逻辑
     */
    private void initProperties() throws IOException {
        // 加载全局配置
        globalPropertiesLoader = new GlobalPropertiesLoader();
        globalPropertiesLoader.loadProperties();

        // 将topic配置文件，加载到内存中
        eagleMqTopicLoader = new EagleMqTopicLoader();
        eagleMqTopicLoader.loadProperties();
        // 开启刷新磁盘线程池
        eagleMqTopicLoader.startRefreshEagleMqTopicInfoTask();

        // 构建topic和MMap对象的映射
        commitLogAppendHandler = new CommitLogAppendHandler();
        for (EagleMqTopicModel eagleMqTopicModel : CommonCache.getEagleMqTopicModelMap().values()) {
            String topic = eagleMqTopicModel.getTopic();
            commitLogAppendHandler.prepareMMapLoading(topic);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BrokerStartUp brokerStartUp = new BrokerStartUp();
        //加载配置 ，缓存对象的生成
        brokerStartUp.initProperties();
        //模拟初始化文件映射
        String topic = "order_cancel_topic";

        for (int i = 0; i < 50000; i++) {
            brokerStartUp.commitLogAppendHandler.appendMsg(topic, ("this is a content " + i).getBytes());
            TimeUnit.MILLISECONDS.sleep(1);
        }
        System.out.println(brokerStartUp.commitLogAppendHandler.readMsg(topic));

        // 关闭刷新磁盘线程池
        CommonThreadPoolConfig.refreshEagleMqTopicExecutor.shutdown();
    }
}
