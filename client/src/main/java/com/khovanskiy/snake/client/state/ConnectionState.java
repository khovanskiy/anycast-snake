package com.khovanskiy.snake.client.state;

import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author victor
 */
@Slf4j
public class ConnectionState extends State {
    @Override
    public void create() {
        super.create();
        GameWorld gameWorld = GameObject.find(Const.GAME_WORLD);
        gameWorld.findComponent(NetworkComponent.class).connect(new InetSocketAddress("::1", 1111), new NetworkComponent.onConnectListener() {
            @Override
            public void onConnected(TCPConnection connection) {
                log.info("Connected");
                connection.listen(new TCPConnection.Listener() {
                    @Override
                    public void onReceived(Object object) {
                        log.info("Server status: " + object);
                    }

                    @Override
                    public void onDisconnected() {
                        runLater(() -> {
                            setState(ConnectionState.this, InitialState.class);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        });
    }


}
