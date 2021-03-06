package com.khovanskiy.snake.common.model;

import com.badlogic.gdx.math.Vector2;
import com.khovanskiy.snake.common.Direction;
import com.khovanskiy.snake.common.component.TCPConnection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.UUID;

/**
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Player extends Model {
    public Snake snake;
    private long score;
    public Direction direction = Direction.UP;
    public UUID playerId;

    public TCPConnection connection;

    public Player(UUID playerId) {
        this.playerId = playerId;
        this.snake = new Snake(new Vector2(3, 3), 5);
    }

    public void setDirection(Direction direction) {
        if (direction == Direction.UP && this.direction != Direction.DOWN ||
                direction == Direction.DOWN && this.direction != Direction.UP ||
                direction == Direction.LEFT && this.direction != Direction.RIGHT ||
                direction == Direction.RIGHT && this.direction != Direction.LEFT ) {
            this.direction = direction;
        }
    }

    public void update(double dt) {
        snake.move(direction);
    }
}
