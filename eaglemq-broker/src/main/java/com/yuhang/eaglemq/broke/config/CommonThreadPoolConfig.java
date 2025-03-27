package com.yuhang.eaglemq.broke.config;

import java.util.concurrent.*;

public class CommonThreadPoolConfig {

    public static ThreadPoolExecutor refreshEagleMqTopicExecutor =
            new ThreadPoolExecutor(1,
                    1,
                    30,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(10),
                    r -> {
                        Thread thread = new Thread(r);
                        thread.setName("refresh-eagle-mq-topic-config");
                        return thread;
                    });

}
