package com.khovanskiy.snake.server.state;

import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.message.AuthMessage;
import com.khovanskiy.snake.common.message.ClientStatusMessage;
import com.khovanskiy.snake.common.message.ReserveMessage;
import com.khovanskiy.snake.common.message.TokenMessage;
import com.khovanskiy.snake.common.state.State;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author victor
 */
@Slf4j
public class GameplayState extends State {
    NetworkComponent component = new NetworkComponent();

    @Override
    public void create() {
        super.create();
        String serverName = "server-1";
        int port = 1111;
        component.start(port, new NetworkComponent.onAcceptListener() {
            @Override
            public void onConnected() {
                log.info("Сервер " + serverName + " запущен на " + port + " для принятия unicast");
            }

            @Override
            public void onAccept(TCPConnection connection) {
                connection.listen(new TCPConnection.Listener() {
                    @Override
                    public void onReceived(Object object) {
                        if (object instanceof AuthMessage) {
                            AuthMessage message = (AuthMessage) object;
                            log.info("Пользователь " + message.getToken() + " успешно подключился к игровой сессии");
                        } else if (object instanceof ClientStatusMessage) {
                            ClientStatusMessage message = (ClientStatusMessage) object;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        });
        component.start(Const.ANYCAST_PORT, new NetworkComponent.onAcceptListener() {
            @Override
            public void onConnected() {
                log.info("Сервер " + serverName + " запущен на " + Const.ANYCAST_PORT + " для принятия anycast");
            }

            @Override
            public void onAccept(TCPConnection connection) {
                connection.listen(new TCPConnection.Listener() {
                    @Override
                    public void onReceived(Object object) {
                        ReserveMessage message = (ReserveMessage) object;
                        UUID uuid = UUID.randomUUID();
                        connection.send(new TokenMessage(uuid, null, port));
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
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
