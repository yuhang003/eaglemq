package com.yuhang.eaglemq.broke.core;

import java.util.HashMap;
import java.util.Map;

public class MMapFileModelManager {

    private final Map<String, MMapFileModel> mMapFileModelMap = new HashMap<String, MMapFileModel>();

    public void put(String topic, MMapFileModel mMapFileModel) {
        mMapFileModelMap.put(topic, mMapFileModel);
    }

    public MMapFileModel get(String topic) {
        return mMapFileModelMap.get(topic);
    }
}
