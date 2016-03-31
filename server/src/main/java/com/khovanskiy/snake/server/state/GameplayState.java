package com.khovanskiy.snake.server.state;

import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.message.AuthMessage;
import com.khovanskiy.snake.common.message.ClientStatusMessage;
import com.khovanskiy.snake.common.message.TokenMessage;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.model.Player;
import com.khovanskiy.snake.common.state.State;
import com.khovanskiy.snake.server.model.ServerSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;

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
    private ServerSession session;

    @Override
    public void create() {
        super.create();
        session = GameObject.create(Const.GAME_WORLD, ServerSession.class);
        session.setServerName(getBundle().getExtra(GameplayState.SERVER_NAME));
        session.setAddress(getBundle().getExtra(GameplayState.ADDRESS));
        session.setPort(getBundle().getExtra(GameplayState.PORT));
        session.setAnycastAddress(getBundle().getExtra(ANYCAST_ADDRESS));
        session.setAnycastPort(getBundle().getExtra(ANYCAST_PORT));

        session.setGameWorld(new GameWorld());

        component.start(session.getAddress(), session.getPort(), new NetworkComponent.onAcceptListener() {
            @Override
            public void onConnected() {
                runLater(() -> {
                    log.info("Сервер " + session.getServerName() + " запущен на " + session.getAddress() + " [" + session.getPort() + "] для принятия unicast");
                });
            }

            @Override
            public void onAccept(TCPConnection connection) {
                connection.listen().subscribe(o -> {
                    runLater(() -> {
                        if (o instanceof AuthMessage) {
                            AuthMessage message = (AuthMessage) o;
                            log.info("Пользователь " + message.getToken() + " успешно подключился к игровой сессии");
                            savePlayer(message.getToken(), connection);
                        } else if (o instanceof ClientStatusMessage) {
                            ClientStatusMessage message = (ClientStatusMessage) o;

                            Player player = session.getConnectionToPlayerMap().get(connection);
                            if (player != null) {
                                player.direction = message.getDirection();
                            }

                        }
                    });
                }, e -> {
                    runLater(() -> {
                        log.error(e.getMessage(), e);
                        deletePlayer(connection);
                    });
                });
            }
        });
        component.start(session.getAnycastAddress(), session.getAnycastPort(), new NetworkComponent.onAcceptListener() {
            @Override
            public void onConnected() {
                runLater(() -> {
                    log.info("Сервер " + session.getServerName() + " запущен на " + session.getAnycastAddress() + " [" + session.getAnycastPort() + "] для принятия anycast");
                });
            }

            @Override
            public void onAccept(TCPConnection connection) {
                connection.listen().subscribe(object -> {
                    UUID uuid = UUID.randomUUID();
                    connection.send(new TokenMessage(uuid, session.getAddress(), session.getPort(), session.getServerName()));
                }, e -> {
                    log.error(e.getMessage(), e);
                });
            }
        });
    }

    private void deletePlayer(TCPConnection connection) {
        Player player = session.getConnectionToPlayerMap().get(connection);
        // удалим модель игрока из игрового мира
        session.getGameWorld().getPlayers().remove(player.getPlayerId());
        // удалим соединение с игроком
        session.getConnectionToPlayerMap().remove(connection);
    }

    private void savePlayer(UUID playerId, TCPConnection connection) {
        Player player = session.getGameWorld().getPlayers().get(playerId);
        if (player == null) {
            player = new Player(playerId);
            session.getGameWorld().getPlayers().put(playerId, player);
            session.getClientConnections().put(playerId, connection);
            session.getConnectionToPlayerMap().put(connection, player);
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        session.getGameWorld().update(dt);
        GameWorld snapshot = SerializationUtils.clone(session.getGameWorld());
        session.allConnections().forEach(connection -> {
            connection.send(snapshot);
        });
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
