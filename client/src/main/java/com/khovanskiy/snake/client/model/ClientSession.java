package com.khovanskiy.snake.client.model;

import com.badlogic.gdx.math.Vector2;
import com.khovanskiy.snake.common.Direction;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.model.Snake;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientSession extends GameObject {
    /**
     * Полученный от сервера токен
     */
    public UUID token;
    /**
     * IP адресс сервера
     */
    public InetAddress address;
    /**
     * Порт сервера
     */
    public int port;
    /**
     * Соединение с сервером
     */
    private TCPConnection serverConnection;
    /**
     * Имя сервера
     */
    private String serverName;

    public Direction prevDirection;
    public Direction direction = Direction.UP;

    private GameWorld gameWorld;

}
