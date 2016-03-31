package com.khovanskiy.snake.common.model;

import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GameWorld extends GameObject {
    public static final int SCORE_PER_APPLE = 100;
    public static final int WORLD_WIDTH = 40;
    public static final int WORLD_HEIGHT = 30;
    private Map<UUID, Player> players = new HashMap<>();
    private Map<UUID, Apple> apples = new HashMap<>();
    private Map<UUID, Brick> bricks = new HashMap<>();

    public void update(double dt) {
        if (!players.isEmpty()) {
            if (RandomUtils.nextInt(0, 10) % 5 == 0) {
                UUID appleId = UUID.randomUUID();
                apples.put(appleId, new Apple(appleId, new Vector2(RandomUtils.nextInt(0, WORLD_WIDTH), RandomUtils.nextInt(0, WORLD_HEIGHT))));
            }
        }
        allPlayers().forEach(player -> player.update(dt));
        Set<UUID> applesToDelete = new HashSet<>();
        allPlayers().forEach(player -> {
            allApples().forEach(apple -> {
                if (player.getSnake().getHeadPosition().equals(apple.getPosition())) {
                    player.setScore(player.getScore() + SCORE_PER_APPLE);
                    applesToDelete.add(apple.getAppleId());
                    player.getSnake().increase();
                }
            });
        });
        applesToDelete.forEach(apples::remove);
    }

    public Collection<Player> allPlayers() {
        return players.values();
    }

    public Collection<Brick> allBricks() {
        return bricks.values();
    }

    public Collection<Apple> allApples() {
        return apples.values();
    }
}
