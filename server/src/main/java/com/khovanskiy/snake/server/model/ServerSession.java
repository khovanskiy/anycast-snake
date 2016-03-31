package com.khovanskiy.snake.server.model;

import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.model.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.InetAddress;
import java.util.*;

/**
 * Сессия игры на стороне сервера
 *
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServerSession extends GameObject {
    private String serverName;
    private InetAddress address;
    private int port;
    private InetAddress anycastAddress;
    private int anycastPort;
    private Map<UUID, TCPConnection> clientConnections = new HashMap<>();
    private Map<TCPConnection, Player> connectionToPlayerMap = new IdentityHashMap<>();

    private GameWorld gameWorld;

    public Collection<TCPConnection> allConnections() {
        return clientConnections.values();
    }
}
