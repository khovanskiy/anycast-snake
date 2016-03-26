package com.khovanskiy.snake.client.model;

import com.badlogic.gdx.math.Vector2;
import com.khovanskiy.snake.common.Direction;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.model.Snake;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author victor
 */
public class ClientGameWorld extends GameWorld {
    public UUID token;
    public InetAddress address;
    public int port;
    public Direction direction = Direction.UP;
    public Snake snake = new Snake(new Vector2(1, 1), 5);
}
