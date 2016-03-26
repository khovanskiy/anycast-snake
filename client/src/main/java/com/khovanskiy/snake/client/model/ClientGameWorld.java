package com.khovanskiy.snake.client.model;

import com.khovanskiy.snake.common.model.GameWorld;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author victor
 */
public class ClientGameWorld extends GameWorld {
    public UUID token;
    public InetAddress address;
    public int poty;
}
