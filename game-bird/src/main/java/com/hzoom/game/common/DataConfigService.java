package com.hzoom.game.common;

import java.util.HashMap;
import java.util.Map;

public class DataConfigService {
    private Map<String, Map<String,Object>> dataConfigMap = new HashMap<>();
    /**
     * 初始化配置表，根据自己的业务需求自己实现即可
     */
    public void init() {

    }

    @SuppressWarnings("unchecked")
    public <T> T getDataConfig(String id,Class<T> clazz) {
        String key = clazz.getName();
        Map<String, Object> valueMap = this.dataConfigMap.get(key);
        if(valueMap == null) {
            return null;
        }
        Object value = valueMap.get(id);
        if(value == null) {
            return null;
        }
        return (T)value;
    }
}
