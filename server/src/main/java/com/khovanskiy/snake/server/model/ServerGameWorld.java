package com.khovanskiy.snake.server.model;

import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.model.Player;
import lombok.Getter;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author victor
 */
@Getter
public class ServerGameWorld extends GameWorld {
    public String serverName;
    public InetAddress address;
    public int port;
    public InetAddress anycastAddress;
    public int anycastPort;
    public Map<UUID, Player> uuidPlayerMap = new HashMap<>();
    public Map<TCPConnection, Player> connectionPlayerMap = new IdentityHashMap<>();
}
