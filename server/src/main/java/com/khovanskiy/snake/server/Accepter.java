package com.khovanskiy.snake.server;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author victor
 */
@Slf4j
public class Accepter implements Runnable {
    public static final int MAX_ATTEMPTS_COUNT = 5;
    private final int port;
    private volatile boolean isRunning = true;
    private Listener listener;

    public Accepter(int port, Listener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        int attempt = 0;
        while (isRunning && attempt < MAX_ATTEMPTS_COUNT) {
            try (ServerSocket accepter = new ServerSocket(port)) {
                attempt = 0;
                Socket socket = accepter.accept();
                log.info("Принят новый клиент = " + socket);
                if (listener != null) {
                    listener.onAccept(socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
                ++attempt;
            }
        }
    }

    public static abstract class Listener {
        public abstract void onAccept(Socket socket);
    }
}
