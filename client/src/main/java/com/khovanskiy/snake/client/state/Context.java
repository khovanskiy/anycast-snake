package com.khovanskiy.snake.client.state;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author victor
 */
public class Context {
    public State currentState;
    public static final int MAX_COUNT = 5;
    private final Queue<Runnable> queue = new ArrayDeque<>();

    public <T extends State> void startState(State current, Class<T> clazz) {
        T newState;
        try {
            newState = clazz.newInstance();
            newState.setContext(this);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        assert currentState == current;
        if (current != null) {
            current.dispose();
        }
        currentState = newState;
        newState.create();
    }

    public void update(double dt) {
        synchronized (queue) {
            int count = 0;
            while (!queue.isEmpty() && count < MAX_COUNT) {
                queue.poll().run();
                ++count;
            }
        }
        currentState.update(dt);
    }

    public void runLater(Runnable runnable) {
        synchronized (queue) {
            queue.add(runnable);
        }
    }
}
