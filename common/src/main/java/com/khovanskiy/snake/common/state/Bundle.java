package com.khovanskiy.snake.common.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * victor
 */
public class Bundle {
    private final Map<String, Object> objects = new HashMap<>();

    public void putExtra(String key, Object value) {
        objects.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key) {
        return (T) objects.get(key);
    }

    public int getIntExtra(String key, int defaultValue) {
        return (int) objects.getOrDefault(key, defaultValue);
    }

    public String getStringExtra(String key) {
        return (String) objects.getOrDefault(key, "");
    }

    public String getStringExtra(String key, String defaultValue) {
        return (String) objects.getOrDefault(key, defaultValue);
    }

    public Serializable getSerializableExtra(String key) {
        return (Serializable) objects.get(key);
    }
}
