package com.khovanskiy.snake.common.model;

import com.khovanskiy.snake.common.component.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author victor
 */
public class GameObject {
    private static final Map<String, GameObject> objects = new HashMap<>();
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    public void addComponent(Component component) {
        components.put(component.getClass(), component);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T findComponent(Class<T> clazz) {
        T component = (T) components.get(clazz);
        assert component != null;
        return component;
    }

    @SuppressWarnings("unchecked")
    public static <T extends GameObject> T create(String id, Class<T> clazz) {
        try {
            synchronized (objects) {
                T t = clazz.newInstance();
                objects.put(id, t);
                return t;
            }
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends GameObject> T find(String id) {
        synchronized (objects) {
            return (T) objects.get(id);
        }
    }
}
