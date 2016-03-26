package com.khovanskiy.snake.server;

import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;

/**
 * @author victor
 */
public class GamePlayBehaviour {

    public void update(double dt) {
        GameWorld world = GameObject.find(Const.GAME_WORLD);
        world.players.values().forEach(player -> {
            player.update(dt);
        });
    }

}
