package com.khovanskiy.snake.common.model;

import com.badlogic.gdx.math.Vector2;
import com.khovanskiy.snake.common.Direction;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author victor
 */
@Data
public class Snake implements Serializable {
    private List<Part> parts;

    public Snake(Vector2 position, int length) {
        assert length > 0;
        this.parts = new ArrayList<>(length);
        Vector2 current = position.cpy();
        for (int i = 0; i < length; ++i) {
            this.parts.add(new Part(current));
            current.y++;
        }
    }

    private Snake() {

    }

    public Snake snapshot() {
        Snake other = new Snake();
        other.parts = new ArrayList<>();
        other.parts.addAll(this.parts.stream().map(Part::copy).collect(Collectors.toList()));
        return other;
    }

    public int getLength() {
        return parts.size();
    }

    public List<Part> getParts() {
        return parts;
    }

    public void move(Direction direction) {
        int dx = 0;
        int dy = 0;
        switch (direction) {
            case UP:++dy;
                break;
            case DOWN:--dy;
                break;
            case LEFT:--dx;
                break;
            case RIGHT:++dx;
                break;
        }
        for (int i = parts.size() - 1; i > 0; --i) {
            parts.get(i).x(parts.get(i - 1).x());
            parts.get(i).y(parts.get(i - 1).y());
        }
        parts.get(0).x(parts.get(0).x() + dx);
        parts.get(0).y(parts.get(0).y() + dy);
    }

    /**
     * @return позицию головы
     */
    public Vector2 getPosition() {
        return parts.get(0).position;
    }

    @Data
    public class Part implements Serializable {
        private final Vector2 position;

        public Part copy() {
            return new Part(position);
        }

        public Part(Vector2 position) {
            this.position = position.cpy();
        }

        public float x() {
            return position.x;
        }

        public void x(float x) {
            position.x = x;
        }

        public float y() {
            return position.y;
        }

        public void y(float y) {
            position.y = y;
        }
    }
}
