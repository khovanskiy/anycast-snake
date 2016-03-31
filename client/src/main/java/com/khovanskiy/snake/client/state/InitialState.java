package com.khovanskiy.snake.client.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.khovanskiy.snake.client.model.ClientGameWorld;
import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.message.ReserveMessage;
import com.khovanskiy.snake.common.message.TokenMessage;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.state.State;
import lombok.extern.slf4j.Slf4j;
import rx.functions.Action1;

import java.net.InetSocketAddress;

/**
 * @author victor
 */
@Slf4j
public class InitialState extends State {
    public static final long MAX_TIMEOUT = 3000;
    private long timestamp;
    private NetworkComponent networkComponent;

    @Override
    public void create() {
        super.create();
        GameWorld gameWorld = GameObject.create(Const.GAME_WORLD, ClientGameWorld.class);
        networkComponent = new NetworkComponent();
        gameWorld.addComponent(networkComponent);
        sendReserveMessage();
    }

    private void sendReserveMessage() {
        log.info("Отправляем anycast запрос на подключение к серверу");
        timestamp = System.currentTimeMillis();
        networkComponent.connect(Const.ANYCAST_ADDRESS).subscribe(connection -> {
            log.info("Подключение по anycast произошло");
            connection.listen(new TCPConnection.Listener() {
                @Override
                public void onReceived(Object object) {
                    TokenMessage tokenMessage = (TokenMessage) object;
                    log.info("Получен токен = " + tokenMessage + " - закрываем временное соединение");
                    connection.close();
                    runLater(() -> {
                        ClientGameWorld gameWorld = GameObject.find(Const.GAME_WORLD);
                        gameWorld.token = tokenMessage.getToken();
                        setState(InitialState.this, ConnectionState.class);
                    });
                }
            }).send(new ReserveMessage());
        }, e -> {
            log.error(e.getMessage(), e);
        });
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (System.currentTimeMillis() > timestamp + MAX_TIMEOUT) {
            log.info("Ни один сервер не ответил в течение " + MAX_TIMEOUT + " ms");
            sendReserveMessage();
        }
    }

    @Override
    public void render() {
        super.render();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
