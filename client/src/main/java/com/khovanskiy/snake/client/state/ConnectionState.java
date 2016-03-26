package com.khovanskiy.snake.client.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.khovanskiy.snake.client.model.ClientGameWorld;
import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.Direction;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.component.TCPConnection;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.Snake;
import com.khovanskiy.snake.common.state.State;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author victor
 */
@Slf4j
public class ConnectionState extends State {
    public static final int GRID_SIZE = 20;
    private SpriteBatch batch = new SpriteBatch();
    private Sprite sprite = new Sprite(new Texture(Gdx.files.internal("textures/snake.png")));
    private ClientGameWorld gameWorld;

    @Override
    public void create() {
        super.create();
        gameWorld = GameObject.find(Const.GAME_WORLD);
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

    @Override
    public void update(double dt) {
        super.update(dt);
        gameWorld.snake.move(gameWorld.direction);
    }

    @Override
    public void render() {
        super.render();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            gameWorld.direction = Direction.UP;
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            gameWorld.direction = Direction.DOWN;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            gameWorld.direction = Direction.LEFT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            gameWorld.direction = Direction.RIGHT;
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sprite.setOriginCenter();
        sprite.setSize(GRID_SIZE, GRID_SIZE);
        batch.begin();
        for (Snake.Part part : gameWorld.snake.getParts()) {
            sprite.setPosition(part.x() * GRID_SIZE, part.y() * GRID_SIZE);
            sprite.draw(batch);
        }
        batch.end();
    }
}
