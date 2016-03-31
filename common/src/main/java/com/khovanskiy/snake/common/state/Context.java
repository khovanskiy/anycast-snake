package com.khovanskiy.snake.common.state;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author victor
 */
@Slf4j
public class Context {
    public State currentState;
    public static final int MAX_COUNT = 64;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
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
        int count = 0;
        while (count < MAX_COUNT) {
            Runnable runnable = queue.poll();
            if (runnable == null) {
                break;
            }
            runnable.run();
            ++count;
        }
        currentState.update(dt);
    }

    public void render() {
        currentState.render();
    }

    public void runLater(Runnable runnable) {
        queue.add(runnable);
    }
}
