package com.khovanskiy.snake.server.state;

import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.message.AuthMessage;
import com.khovanskiy.snake.common.message.ClientStatusMessage;
import com.khovanskiy.snake.common.message.TokenMessage;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.Player;
import com.khovanskiy.snake.common.state.State;
import com.khovanskiy.snake.server.model.ServerGameWorld;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author victor
 */
@Slf4j
public class GameplayState extends State {
    /**
     * Имя сервера
     */
    public static final String SERVER_NAME = "hostname";
    /**
     * Адресс для подключения игроков
     */
    public static final String ADDRESS = "address";
    /**
     * Порт для подключения игроков
     */
    public static final String PORT = "port";
    /**
     * Адресс для подключения игроков
     */
    public static final String ANYCAST_ADDRESS = "anycast_address";
    /**
     * Порт для подключения игроков
     */
    public static final String ANYCAST_PORT = "anycast_port";


    NetworkComponent component = new NetworkComponent();
    ServerGameWorld world;

    @Override
    public void create() {
        super.create();
        world = GameObject.create(Const.GAME_WORLD, ServerGameWorld.class);
        world.serverName = getBundle().getExtra(GameplayState.SERVER_NAME);
        world.address = getBundle().getExtra(GameplayState.ADDRESS);
        world.port = getBundle().getExtra(GameplayState.PORT);
        world.anycastAddress = getBundle().getExtra(ANYCAST_ADDRESS);
        world.anycastPort = getBundle().getExtra(ANYCAST_PORT);

        component.start(world.getAddress(), world.getPort(), new NetworkComponent.onAcceptListener() {
            @Override
            public void onConnected() {
                runLater(() -> {
                    log.info("Сервер " + world.getServerName() + " запущен на " + world.getAddress() + " [" + world.getPort() + "] для принятия unicast");
                });
            }

            @Override
            public void onAccept(TCPConnection connection) {
                connection.listen(new TCPConnection.Listener() {
                    @Override
                    public void onReceived(Object object) {
                        runLater(() -> {
                            if (object instanceof AuthMessage) {
                                AuthMessage message = (AuthMessage) object;
                                log.info("Пользователь " + message.getToken() + " успешно подключился к игровой сессии");

                                Player player = world.getUuidPlayerMap().get(message.getToken());
                                if (player == null) {
                                    player = new Player(message.getToken());
                                    world.uuidPlayerMap.put(message.getToken(), player);
                                }
                                player.connection = connection;
                                world.connectionPlayerMap.put(connection, player);

                            } else if (object instanceof ClientStatusMessage) {
                                ClientStatusMessage message = (ClientStatusMessage) object;

                                Player player = world.connectionPlayerMap.get(connection);
                                if (player != null) {
                                    player.direction = message.getDirection();
                                }

                            }
                        });
                    }

                    @Override
                    public void onDisconnected() {
                        runLater(() -> {
                            Player player = world.connectionPlayerMap.get(connection);
                            world.uuidPlayerMap.remove(player.token);
                            world.connectionPlayerMap.remove(connection);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runLater(() -> {
                            log.error(e.getMessage(), e);
                        });
                    }
                });
            }
        });
        component.start(world.getAnycastAddress(), world.getAnycastPort(), new NetworkComponent.onAcceptListener() {
            @Override
            public void onConnected() {
                runLater(() -> {
                    log.info("Сервер " + world.getServerName() + " запущен на " + world.getAnycastAddress() + " [" + world.getAnycastPort() + "] для принятия anycast");
                });
            }

            @Override
            public void onAccept(TCPConnection connection) {
                connection.listen(new TCPConnection.Listener() {
                    @Override
                    public void onReceived(Object object) {
                        UUID uuid = UUID.randomUUID();
                        connection.send(new TokenMessage(uuid, null, world.getPort()));
                    }

                    @Override
                    public void onError(Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        });
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        world.uuidPlayerMap.values().forEach(player -> {
            player.update(dt);
        });
        world.uuidPlayerMap.values().forEach(player -> {
            player.connection.send(player.snake.snapshot());
        });
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
