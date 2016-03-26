package com.khovanskiy.snake.server;

import com.khovanskiy.snake.common.state.Context;
import com.khovanskiy.snake.server.state.GameplayState;
import lombok.extern.slf4j.Slf4j;

/**
 * @author victor
 */
@Slf4j
public class ServerApplication implements Runnable {
    private final String serverName;
    private final int port;
    private final Context context = new Context();

    public static void main(String[] args) {
        new ServerApplication(args[0], Integer.parseInt(args[1])).run();
    }

    public ServerApplication(String serverName, int port) {
        assert serverName.length() > 0 : "Должно быть задано имя сервера";
        assert port > 1024 : "Порт сервера должен быть больше 1024";
        this.serverName = serverName;
        this.port = port;
    }

    public void run() {
        context.startState(null, GameplayState.class);
        while (true) {
            context.update(1d);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
