package com.khovanskiy.snake.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GameWorld extends GameObject {
    private Map<UUID, Player> players = new HashMap<>();

    public void update(double dt) {
        allPlayers().forEach(player -> player.update(dt));
    }

    public Collection<Player> allPlayers() {
        return players.values();
    }
}
