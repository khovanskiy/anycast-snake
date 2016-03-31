package com.khovanskiy.snake.client.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.khovanskiy.snake.client.model.ClientSession;
import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.Direction;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.message.AuthMessage;
import com.khovanskiy.snake.common.message.ClientStatusMessage;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.model.Snake;
import com.khovanskiy.snake.common.state.State;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author victor
 */
@Slf4j
public class GameplayState extends State {
    public static final int GRID_SIZE = 20;
    private SpriteBatch batch = new SpriteBatch();
    private Sprite sprite = new Sprite(new Texture(Gdx.files.internal("textures/snake.png")));
    private ClientSession session;

    @Override
    public void create() {
        super.create();
        session = GameObject.find(Const.GAME_WORLD);
        session.findComponent(NetworkComponent.class).connect(new InetSocketAddress(session.getAddress(), session.getPort())).subscribe(connection -> {
            log.info("Подключились к серверу " + session.getServerName());
            session.setServerConnection(connection);
            connection.listen().subscribe(object -> {
                //log.info("Server status: " + object);
                runLater(() -> {
                    session.setGameWorld((GameWorld) object);
                });
            }, e -> {
                log.error(e.getMessage(), e);
                runLater(() -> {
                    setState(GameplayState.this, InitialState.class);
                });
            });
            connection.send(new AuthMessage(session.getToken()));
        }, e -> {
            log.error(e.getMessage(), e);
        });
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (session.getServerConnection() != null) {
            session.getServerConnection().send(new ClientStatusMessage(session.direction));
        }
    }

    @Override
    public void render() {
        super.render();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            session.direction = Direction.UP;
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            session.direction = Direction.DOWN;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            session.direction = Direction.LEFT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            session.direction = Direction.RIGHT;
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sprite.setOriginCenter();
        sprite.setSize(GRID_SIZE, GRID_SIZE);
        batch.begin();
        if (session.getGameWorld() != null) {
            session.getGameWorld().allPlayers().forEach(player -> {
                if (player.getSnake() != null) {
                    for (Snake.Part part : player.getSnake().getParts()) {
                        sprite.setPosition(part.x() * GRID_SIZE, part.y() * GRID_SIZE);
                        sprite.draw(batch);
                    }
                }
            });
        }
        batch.end();
    }
}
