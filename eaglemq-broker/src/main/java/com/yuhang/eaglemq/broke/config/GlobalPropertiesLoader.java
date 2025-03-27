package com.yuhang.eaglemq.broke.config;

import com.yuhang.eaglemq.broke.cache.CommonCache;
import io.netty.util.internal.StringUtil;

import static com.yuhang.eaglemq.broke.constants.BrokerConstants.EAGLE_MQ_HOME;

/**
 * 加载所有的GlobalProperties
 */
public class GlobalPropertiesLoader {

    public void loadProperties() {
        GlobalProperties globalProperties = new GlobalProperties();
//        String eagleMqHome = System.getenv(EAGLE_MQ_HOME);
        String eagleMqHome = "D:\\Code\\JAVA\\mianshi_2025\\eaglemq\\borker";
        if (StringUtil.isNullOrEmpty(eagleMqHome)) {
            throw new RuntimeException("EAGLE_MQ_HOME is null");
        }
        globalProperties.setEagleMqHome(eagleMqHome);
        CommonCache.setGlobalProperties(globalProperties);
    }
}
