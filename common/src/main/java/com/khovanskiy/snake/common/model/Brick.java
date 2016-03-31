package com.khovanskiy.snake.common.model;

import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Brick extends Model {
    private final UUID appleId;
    private final Vector2 position;

    public Brick(UUID appleId, Vector2 position) {
        this.appleId = appleId;
        this.position = position;
    }

    public float x() {
        return position.x;
    }

    public float y() {
        return position.y;
    }
}
