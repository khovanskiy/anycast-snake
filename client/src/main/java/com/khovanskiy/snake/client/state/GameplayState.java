package com.khovanskiy.snake.client.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.khovanskiy.snake.client.model.ClientSession;
import com.khovanskiy.snake.common.Const;
import com.khovanskiy.snake.common.Direction;
import com.khovanskiy.snake.common.component.NetworkComponent;
import com.khovanskiy.snake.common.message.AuthMessage;
import com.khovanskiy.snake.common.message.ClientStatusMessage;
import com.khovanskiy.snake.common.model.GameObject;
import com.khovanskiy.snake.common.model.GameWorld;
import com.khovanskiy.snake.common.model.Player;
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
    private Texture backgroundTexture = new Texture(Gdx.files.internal("textures/background.png"));
    private Texture brickTexture = new Texture(Gdx.files.internal("textures/brick.png"));
    private Texture snakeTexture = new Texture(Gdx.files.internal("textures/snake-graphics.png"));
    public static final int TILE_WIDTH = 64;
    public static final int TILE_HEIGHT = 64;

    private Sprite backgroundView = new Sprite(backgroundTexture);

    private Sprite brickView = new Sprite(brickTexture);
    {
        brickView.setOriginCenter();
        brickView.setSize(GRID_SIZE, GRID_SIZE);
    }
    private Sprite appleView = new Sprite(snakeTexture, 0, 192, TILE_WIDTH, TILE_HEIGHT);
    {
        appleView.setOriginCenter();
        appleView.setSize(GRID_SIZE, GRID_SIZE);
    }

    private Sprite headUp = new Sprite(snakeTexture, 192, 0, TILE_WIDTH, TILE_HEIGHT);
    private Sprite headDown = new Sprite(snakeTexture, 256, 64, TILE_WIDTH, TILE_HEIGHT);
    private Sprite headLeft = new Sprite(snakeTexture, 192, 64, TILE_WIDTH, TILE_HEIGHT);
    private Sprite headRight = new Sprite(snakeTexture, 256, 0, TILE_WIDTH, TILE_HEIGHT);

    private Sprite tailDown = new Sprite(snakeTexture, 192, 128, TILE_WIDTH, TILE_HEIGHT);
    private Sprite tailUp = new Sprite(snakeTexture, 256, 192, TILE_WIDTH, TILE_HEIGHT);
    private Sprite tailLeft = new Sprite(snakeTexture, 192, 192, TILE_WIDTH, TILE_HEIGHT);
    private Sprite tailRight = new Sprite(snakeTexture, 256, 128, TILE_WIDTH, TILE_HEIGHT);

    private Sprite horizontal = new Sprite(snakeTexture, 64, 0, TILE_WIDTH, TILE_HEIGHT);
    private Sprite vertical = new Sprite(snakeTexture, 128, 64, TILE_WIDTH, TILE_HEIGHT);

    private Sprite downRight = new Sprite(snakeTexture, 0, 64, TILE_WIDTH, TILE_HEIGHT);
    private Sprite rightUp = new Sprite(snakeTexture, 0, 0, TILE_WIDTH, TILE_HEIGHT);

    private Sprite topLeft  = new Sprite(snakeTexture, 128, 0, TILE_WIDTH, TILE_HEIGHT);
    private Sprite leftDown = new Sprite(snakeTexture, 128, 128, TILE_WIDTH, TILE_HEIGHT);

    private BitmapFont font = new BitmapFont();
    {
        font.setColor(new Color(0xffcc00ff));
    }
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
            log.error(e.getMessage());
        });
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (session.getServerConnection() != null && session.prevDirection != session.direction) {
            session.getServerConnection().send(new ClientStatusMessage(session.direction));
            session.prevDirection = session.direction;
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
        batch.begin();

        if (session.getGameWorld() != null) {
            backgroundView.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            backgroundView.draw(batch);

            session.getGameWorld().allBricks().forEach(brick -> {
                brickView.setPosition(brick.x() * GRID_SIZE, brick.y() * GRID_SIZE);
                brickView.draw(batch);
            });

            session.getGameWorld().allPlayers().forEach(player -> {
                if (player.getSnake() != null) {
                    for (int i = 0; i < player.getSnake().getParts().size(); ++i) {
                        Snake.Part segment = player.getSnake().getParts().get(i);
                        if (i == 0) {
                            switch (player.getDirection()) {
                                case UP:
                                    assign(segment, headUp, batch);
                                    break;
                                case DOWN:
                                    assign(segment, headDown, batch);
                                    break;
                                case LEFT:
                                    assign(segment, headLeft, batch);
                                    break;
                                case RIGHT:
                                    assign(segment, headRight, batch);
                                    break;
                            }
                        } else if (i == player.getSnake().getParts().size() - 1) {
                            Snake.Part pseg = player.getSnake().getParts().get(i - 1);
                            if (pseg.y() < segment.y()) {
                                // Up
                                assign(segment, tailUp, batch);
                            } else if (pseg.x() > segment.x()) {
                                // Right
                                assign(segment, tailRight, batch);
                            } else if (pseg.y() > segment.y()) {
                                // Down
                                assign(segment, tailDown, batch);
                            } else if (pseg.x() < segment.x()) {
                                // Left
                                assign(segment, tailLeft, batch);
                            }
                        } else {
                            Snake.Part pseg = player.getSnake().getParts().get(i - 1); // Previous segment
                            Snake.Part nseg = player.getSnake().getParts().get(i + 1);; // Next segment
                            if (pseg.x() < segment.x() && nseg.x() > segment.x() || nseg.x() < segment.x() && pseg.x() > segment.x()) {
                                // Horizontal Left-Right
                                assign(segment, horizontal, batch);
                            } else if (pseg.x() < segment.x() && nseg.y() > segment.y() || nseg.x() < segment.x() && pseg.y() > segment.y()) {
                                // Angle Left-Down
                                assign(segment, leftDown, batch);
                            } else if (pseg.y() < segment.y() && nseg.y() > segment.y() || nseg.y() < segment.y() && pseg.y() > segment.y()) {
                                // Vertical Up-Down
                                assign(segment, vertical, batch);
                            } else if (pseg.y() < segment.y() && nseg.x() < segment.x() || nseg.y() < segment.y() && pseg.x() < segment.x()) {
                                // Angle Top-Left
                                assign(segment, topLeft, batch);
                            } else if (pseg.x() > segment.x() && nseg.y() < segment.y() || nseg.x() > segment.x() && pseg.y() < segment.y()) {
                                // Angle Right-Up
                                assign(segment, rightUp, batch);
                            } else if (pseg.y() > segment.y() && nseg.x() > segment.x() || nseg.y() > segment.y() && pseg.x() > segment.x()) {
                                // Angle Down-Right
                                assign(segment, downRight, batch);
                            }
                        }
                    }
                }
            });
            session.getGameWorld().allApples().forEach(apple -> {
                appleView.setPosition(apple.x() * GRID_SIZE, apple.y() * GRID_SIZE);
                appleView.draw(batch);
            });
            int p = 0;
            for (Player player : session.getGameWorld().allPlayers()) {
                font.draw(batch, "Player " + player.getPlayerId() + ": " + player.getScore(), 25, Gdx.graphics.getHeight() - 25 - 25 * p);
                ++p;
            }
        }
        batch.end();
    }

    private void assign(Snake.Part segment, Sprite view, SpriteBatch batch) {
        view.setOriginCenter();
        view.setSize(GRID_SIZE, GRID_SIZE);
        view.setPosition(segment.x() * GRID_SIZE, segment.y() * GRID_SIZE);
        view.draw(batch);
    }

    @Override
    public void dispose() {
        font.dispose();
        snakeTexture.dispose();
        super.dispose();
    }
}
