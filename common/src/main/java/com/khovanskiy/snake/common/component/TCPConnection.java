package com.khovanskiy.snake.common.component;

import lombok.extern.slf4j.Slf4j;
import rx.*;
import rx.observables.AsyncOnSubscribe;
import rx.schedulers.Schedulers;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author victor
 */
@Slf4j
public class TCPConnection {
    private static final Scheduler SCHEDULER = Schedulers.from(Executors.newCachedThreadPool());
    private final NetworkComponent component;
    private final Socket socket;
    final InetSocketAddress address;
    private volatile Listener listener;
    private final Subscription sender;
    private final Observable<Object> receiver;
    private LinkedBlockingQueue<Serializable> queue = new LinkedBlockingQueue<>();

    TCPConnection(NetworkComponent component, final InetSocketAddress address, final Socket socket) {
        this.component = component;
        this.address = address;
        this.socket = socket;

        this.sender = Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
            try (ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream())) {
                while (!socket.isClosed() && !subscriber.isUnsubscribed()) {
                    try {
                        Object object = queue.take();
                        stream.writeObject(object);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(SCHEDULER).subscribe();

        this.receiver = Observable.create(subscriber -> {
            try (ObjectInputStream stream = new ObjectInputStream(socket.getInputStream())) {
                while (!socket.isClosed()) {
                    try {
                        Object object = stream.readObject();
                        subscriber.onNext(object);
                    } catch (ClassNotFoundException e) {
                        subscriber.onError(e);
                    }
                }
            } catch (EOFException e) {
                subscriber.onError(e);
            } catch (IOException e) {
                subscriber.onError(e);
            }
            component.remove(TCPConnection.this);
        }).subscribeOn(SCHEDULER);
    }

    public void send(Serializable object) {
        try {
            queue.put(object);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Observable<Object> listen() {
        return receiver;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
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
