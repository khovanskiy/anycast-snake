package com.khovanskiy.snake.common.component;

import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author victor
 */
@Slf4j
public class TCPConnection {
    private final NetworkComponent component;
    private final Socket socket;
    private volatile Listener listener;
    private Sender sender;
    private Receiver receiver;
    private LinkedBlockingQueue<Serializable> queue = new LinkedBlockingQueue<>();

    TCPConnection(NetworkComponent component, Socket socket) {
        this.component = component;
        this.socket = socket;
        this.sender = new Sender();
        new Thread(sender).start();
        this.receiver = new Receiver();
        new Thread(receiver).start();
    }

    public void send(Serializable object) {
        try {
            queue.put(object);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TCPConnection listen(Listener listener) {
        this.listener = listener;
        if (receiver == null) {
            receiver = new Receiver();
            new Thread(receiver).start();
        }
        return this;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private class Sender implements Runnable {
        @Override
        public void run() {
            try (ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream())) {
                while (!socket.isClosed()) {
                    try {
                        Object object = queue.take();
                        log.info("Send: " + object);
                        stream.writeObject(object);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Receiver implements Runnable {

        @Override
        public void run() {
            try (ObjectInputStream stream = new ObjectInputStream(socket.getInputStream())) {
                while (!socket.isClosed()) {
                    try {
                        Object object = stream.readObject();
                        if (listener != null) {
                            listener.onReceived(object);
                        }
                    } catch (ClassNotFoundException e) {
                        if (listener != null) {
                            listener.onError(e);
                        }
                    }
                }
            } catch (EOFException e) {
                if (listener != null) {
                    listener.onDisconnected();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static abstract class Listener {
        public void onReceived(Object object) {

        }

        public void onDisconnected() {

        }

        public void onError(Exception e) {

        }
    }
}
