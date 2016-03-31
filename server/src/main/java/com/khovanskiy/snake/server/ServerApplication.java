package com.khovanskiy.snake.server;

import com.khovanskiy.snake.common.state.Bundle;
import com.khovanskiy.snake.common.state.Context;
import com.khovanskiy.snake.server.state.GameplayState;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * @author victor
 */
@Slf4j
public class ServerApplication implements Runnable {
    private final String serverName;
    private final InetAddress address;
    private final int port;
    private final InetAddress anycastAddress;
    private final int anycastPort;
    private final Context context = new Context();

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            throw new IllegalArgumentException("Количество параметров должно быть равно 5.");
        }
        new ServerApplication(args[0], args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4])).run();
    }

    public ServerApplication(String serverName, String address, int port, String anycastAddress, int anycastPort) throws Exception {
        assert serverName.length() > 0 : "Должно быть задано имя сервера";
        this.serverName = serverName;
        this.address = InetAddress.getByName(address);
        assert port > 1024 : "Порт сервера должен быть больше 1024";
        this.port = port;
        this.anycastAddress = InetAddress.getByName(anycastAddress);
        assert anycastPort > 1024 : "Порт сервера должен быть больше 1024";
        this.anycastPort = anycastPort;
    }

    public void run() {
        Bundle bundle = new Bundle();
        bundle.putExtra(GameplayState.SERVER_NAME, serverName);
        bundle.putExtra(GameplayState.ADDRESS, address);
        bundle.putExtra(GameplayState.PORT, port);
        bundle.putExtra(GameplayState.ANYCAST_ADDRESS, anycastAddress);
        bundle.putExtra(GameplayState.ANYCAST_PORT, anycastPort);
        context.startState(null, GameplayState.class, bundle);
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
