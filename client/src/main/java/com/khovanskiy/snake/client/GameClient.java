package com.khovanskiy.snake.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.khovanskiy.snake.client.state.Context;
import com.khovanskiy.snake.client.state.InitialState;
import lombok.extern.slf4j.Slf4j;

/**
 * @author victor
 */
@Slf4j
public class GameClient extends ApplicationAdapter {
    /**
     * Количество тиков в секунду
     */
    public static final int TICKS_PER_SECOND = 2;
    public static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    public static final int MAX_FRAMESKIP = 20;
    private long nextGameTick;
    private final Context context = new Context();

    @Override
    public void create() {
        super.create();
        nextGameTick = System.currentTimeMillis();
        context.startState(null, InitialState.class);
    }

    @Override
    public void render() {
        super.render();
        int loops = 0;
        while (System.currentTimeMillis() > nextGameTick && loops < MAX_FRAMESKIP) {
            update(1d / TICKS_PER_SECOND);
            nextGameTick += SKIP_TICKS;
            loops++;
        }
    }

    public void update(double dt) {
        context.update(dt);
    }
}