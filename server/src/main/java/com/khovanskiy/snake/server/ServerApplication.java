package com.khovanskiy.snake.server;

import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.message.ReserveMessage;
import com.khovanskiy.snake.common.message.TokenMessage;
import com.khovanskiy.snake.common.model.GameWorld;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author victor
 */
@Slf4j
public class ServerApplication implements Runnable {

    private final String serverName;
    private final int port;

    public static void main(String[] args) {
        new ServerApplication(args[0], Integer.parseInt(args[1])).run();
    }

    public ServerApplication(String serverName, int port) {
        assert serverName.length() > 0 : "Должно быть задано имя сервера";
        assert port > 1024 : "Порт сервера должен быть больше 1024";
        this.serverName = serverName;
        this.port = port;
    }

    public void run() {
        NetworkComponent component = new NetworkComponent();
        component.start(1111, new NetworkComponent.onAcceptListener() {
            @Override
            public void onConnected() {
                log.info("Сервер " + serverName + " запущен на " + port);
            }

            @Override
            public void onAccept(TCPConnection connection) {
                connection.listen(new TCPConnection.Listener() {
                    @Override
                    public void onReceived(Object object) {
                        ReserveMessage message = (ReserveMessage) object;
                        log.info("From client: " + message);
                        UUID uuid = UUID.randomUUID();
                        connection.send(new TokenMessage(uuid, null, 1111));
                    }

                    @Override
                    public void onError(Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        });
        while (true) {
            //gameWorld.update(1000);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
