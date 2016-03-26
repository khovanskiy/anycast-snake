package com.khovanskiy.snake.common;

import java.net.InetSocketAddress;

/**
 * @author victor
 */
public class Const {
    public static final String GAME_WORLD = "game_world";
    public static final String ANYCAST_HOSTNAME = "::1";
    public static final int ANYCAST_PORT = 1234;
    public static final InetSocketAddress ANYCAST_ADDRESS = new InetSocketAddress(ANYCAST_HOSTNAME, ANYCAST_PORT);
}
