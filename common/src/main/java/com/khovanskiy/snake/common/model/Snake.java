package com.khovanskiy.snake.common.model;

import com.badlogic.gdx.math.Vector2;
import com.khovanskiy.snake.common.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author victor
 */
public class Snake {
    private List<Part> parts;
    private Direction direction;

    public Snake(Vector2 position, int length) {
        assert length > 0;
        this.parts = new ArrayList<>(length);
        Vector2 current = position.cpy();
        for (int i = 0; i < length; ++i) {
            this.parts.add(new Part(current));
            current.y++;
        }
        this.direction = Direction.UP;
    }

    public int getLength() {
        return parts.size();
    }

    /**
     * @return позицию головы
     */
    public Vector2 getPosition() {
        return parts.get(0).position;
    }

    public class Part {
        private final Vector2 position;

        public Part(Vector2 position) {
            this.position = position.cpy();
        }
    }
}
