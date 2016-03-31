package com.khovanskiy.snake.common.model;

import com.badlogic.gdx.math.Vector2;
import com.khovanskiy.snake.common.Direction;
import com.khovanskiy.snake.common.component.TCPConnection;

import java.util.UUID;

/**
 * @author victor
 */
public class Player {
    public Snake snake;
    public Direction direction = Direction.UP;
    public UUID token;

    public TCPConnection connection;

    public Player(UUID token) {
        this.token = token;
        snake = new Snake(new Vector2(3, 3), 5);
    }

    public void update(double dt) {
        snake.move(direction);
    }
}
