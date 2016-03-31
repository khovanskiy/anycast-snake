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
        context.startState(state, clazz);
    }

    public void setContext(Context context) {
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
