package com.yuhang.eaglemq.broke.cache;

import com.yuhang.eaglemq.broke.config.GlobalProperties;
import com.yuhang.eaglemq.broke.model.EagleMqTopicModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一缓存对象
 * 用于在其他类中获取一些对象
 */

public class CommonCache {

    @Getter
    @Setter
    private static GlobalProperties globalProperties;

    @Getter
    @Setter
    private static List<EagleMqTopicModel> eagleMqTopicModelList;

    public static Map<String, EagleMqTopicModel> getEagleMqTopicModelMap() {
        return eagleMqTopicModelList.stream().collect(Collectors.toMap(EagleMqTopicModel::getTopic, item -> item));
    }
}
