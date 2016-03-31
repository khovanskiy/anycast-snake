package com.khovanskiy.snake.common.state;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author victor
 */
public abstract class State {
    private Context context;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Bundle bundle;
    private final Thread mainThread;

    public State() {
        mainThread = Thread.currentThread();
    }

    public void create() {
        logger.info("create");
    }

    public void update(double dt) {

    }

    public void render() {

    }

    public void runLater(Runnable runnable) {
        context.runLater(runnable);
    }

    public <T extends State> void setState(State state, Class<T> clazz) {
        assert Thread.currentThread() == mainThread;
        context.startState(state, clazz);
    }

    public void setContext(Context context) {
        assert Thread.currentThread() == mainThread;
        this.context = context;
    }

    public void dispose() {
        logger.info("dispose");
    }

    public void setBundle(@NonNull Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
