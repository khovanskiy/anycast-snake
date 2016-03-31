package com.khovanskiy.snake.common.component;

import com.khovanskiy.snake.common.Const;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Компонент для работы с сетью
 *
 * @author victor
 */
@Slf4j
public class NetworkComponent extends Component {
    public static final int MAX_UDP = 1500;
    public static final int MAX_ATTEMPTS_COUNT = 1;
    private final Map<Integer, Accepter> acceptors = new HashMap<>();
    private final Map<Integer, UDPReceiver> receivers = new HashMap<>();
    private UDPSender sender;
    private static final Scheduler CONNECT_SCHEDULER = Schedulers.from(Executors.newCachedThreadPool());
    private final Map<InetSocketAddress, ConnectAction> connectActionMap = new HashMap<>();


    public NetworkComponent() {
        this.sender = new UDPSender();
        new Thread(sender).start();
    }

    /**
     * Подключиться к указанному адресу
     *
     * @param address адрес
     */
    public Observable<TCPConnection> connect(final InetSocketAddress address) {
        synchronized (connectActionMap) {
            ConnectAction connectAction = connectActionMap.get(address);
            if (connectAction == null) {
                connectAction = new ConnectAction();
                connectActionMap.put(address, connectAction);
            }
            if (connectAction.connection != null) {
                return Observable.just(connectAction.connection).subscribeOn(CONNECT_SCHEDULER);
            }
            if (connectAction.observable == null) {
                connectAction.observable = Observable.create((Observable.OnSubscribe<TCPConnection>) subscriber -> {
                    int attempt = 0;
                    while (attempt < MAX_ATTEMPTS_COUNT) {
                        try {
                            log.info("try to connect " + address);
                            Socket socket = new Socket(address.getAddress(), address.getPort());
                            TCPConnection connection = new TCPConnection(NetworkComponent.this, address, socket);
                            synchronized (connectActionMap) {
                                ConnectAction action = connectActionMap.get(address);
                                assert action != null;
                                action.connection = connection;
                                action.observable = null;
                            }
                            subscriber.onNext(connection);
                            subscriber.onCompleted();
                            return;
                        } catch (IOException e) {
                            ++attempt;
                            subscriber.onError(e);
                        }
                    }
                    if (attempt == MAX_ATTEMPTS_COUNT) {
                        synchronized (connectActionMap) {
                            connectActionMap.remove(address);
                        }
                    }
                }).subscribeOn(CONNECT_SCHEDULER).publish().autoConnect();
            }
            return connectAction.observable;
        }
    }

    /**
     * Удалить TCP-соединение
     *
     * @param connection соединение
     */
    void remove(TCPConnection connection) {
        synchronized (connectActionMap) {
            connectActionMap.remove(connection.address);
        }
    }

    private class ConnectAction {
        Observable<TCPConnection> observable;
        TCPConnection connection;
    }

    /**
     * Начать прослушивание указанного порта по TCP
     *
     * @param port порт
     * @param listener обработчик новых подключений
     */
    public NetworkComponent start(InetAddress address, int port, onAcceptListener listener) {
        synchronized (acceptors) {
            Accepter accepter = acceptors.get(port);
            if (accepter == null) {
                accepter = new Accepter(address, port, listener);
                new Thread(accepter).start();
            }
            return this;
        }
    }

    /**
     * Закончить прослушивание указанного порта по TCP
     *
     * @param port порт
     */
    public NetworkComponent stop(int port) {
        synchronized (acceptors) {
            Accepter accepter = acceptors.get(port);
            if (accepter != null) {
                accepter.setRunning(false);
                acceptors.remove(port);
            }
            return this;
        }
    }

    /**
     * Закончить прослушивание всех портов
     */
    public NetworkComponent stopAll() {
        synchronized (acceptors) {
            acceptors.values().forEach(accepter -> {
                accepter.setRunning(false);
            });
            acceptors.clear();
            return this;
        }
    }

    public NetworkComponent send(InetSocketAddress address, Serializable object) {
        try {
            sender.queue.put(new UDPMessage(address.getAddress(), address.getPort(), object));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public NetworkComponent listen(int port, Listener listener) {
        synchronized (receivers) {
            UDPReceiver receiver = receivers.get(port);
            if (receiver == null) {
                receiver = new UDPReceiver(port, listener);
                new Thread(receiver).start();
            }
            return this;
        }
    }

    @Data
    @AllArgsConstructor
    public class UDPMessage {
        InetAddress address;
        int port;
        Serializable object;
    }

    private class UDPSender implements Runnable {
        private final LinkedBlockingQueue<UDPMessage> queue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                while (!socket.isClosed()) {
                    try {
                        UDPMessage message = queue.take();
                        log.info("UDP Send: " + message);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try (ObjectOutputStream stream = new ObjectOutputStream(bos)) {
                            stream.writeObject(message.object);
                            byte[] bytes = bos.toByteArray();
                            log.info("Object: " + bytes.length + " bytes");
                            assert bytes.length < MAX_UDP;
                            DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, message.address, message.port);
                            socket.send(packet);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static abstract class Listener {
        public void onReceived(InetAddress address, Object object) {

        }

        public void onDisconnected() {

        }

        public void onError(Exception e) {

        }
    }

    private class UDPReceiver implements Runnable {
        private final int port;
        private final Listener listener;

        public UDPReceiver(int port, Listener listener) {
            this.port = port;
            this.listener = listener;
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                while (!socket.isClosed()) {
                    byte[] bytes = new byte[MAX_UDP];
                    DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length);
                    socket.receive(packet);
                    //log.info("Received packet: " + packet.getAddress() + " " + packet.getPort());
                    ByteArrayInputStream bos = new ByteArrayInputStream(bytes, 0, bytes.length);
                    try (ObjectInputStream stream = new ObjectInputStream(bos)) {
                        try {
                            Object object = stream.readObject();
                            if (listener != null) {
                                listener.onReceived(packet.getAddress(), object);
                            }
                        } catch (ClassNotFoundException e) {
                            if (listener != null) {
                                listener.onError(e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Accepter implements Runnable {
        private final InetAddress address;
        private final int port;
        private volatile boolean isRunning = true;
        private onAcceptListener listener;

        public Accepter(InetAddress address, int port, onAcceptListener listener) {
            this.address = address;
            this.port = port;
            this.listener = listener;
        }

        @Override
        public void run() {
            int attempt = 0;
            while (isRunning && attempt < MAX_ATTEMPTS_COUNT) {
                try (ServerSocket accepter = new ServerSocket(port, 50, address)) {
                    attempt = 0;
                    listener.onConnected();
                    while (!accepter.isClosed()) {
                        Socket socket = accepter.accept();
                        log.info("Принят новый клиент = " + socket);
                        listener.onAccept(new TCPConnection(NetworkComponent.this, null, socket));
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    ++attempt;
                }
            }
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        public boolean isRunning() {
            return isRunning;
        }
    }

    public static abstract class onConnectListener {
        public abstract void onConnected(TCPConnection connection);
    }

    public static abstract class onAcceptListener {
        public void onConnected() {

        }

        public abstract void onAccept(TCPConnection connection);
    }
}
