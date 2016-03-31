package com.khovanskiy.snake.common.state;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author victor
 */
public class Context {
    public State currentState;
    public static final int MAX_COUNT = 64;
    private final Queue<Runnable> queue = new ArrayDeque<>();
    private final Thread mainThread;

    public Context() {
        mainThread = Thread.currentThread();
    }

    public <T extends State> void startState(State current, Class<T> clazz) {
        startState(current, clazz, new Bundle());
    }

    public <T extends State> void startState(State current, Class<T> clazz, Bundle bundle) {
        assert Thread.currentThread() == mainThread : "Current thread = " + Thread.currentThread() + ", main thread = " + mainThread;
        T newState;
        try {
            newState = clazz.newInstance();
            newState.setBundle(bundle);
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

    public void render() {
        currentState.render();
    }

    public void runLater(Runnable runnable) {
        synchronized (queue) {
            queue.add(runnable);
        }
    }
}
