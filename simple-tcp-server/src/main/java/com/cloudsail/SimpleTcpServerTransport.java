package com.cloudsail;

import com.cloudsail.exception.SendException;
import com.cloudsail.exception.StartFailException;
import com.cloudsail.handler.TransportHandler;
import com.cloudsail.message.Message;
import com.cloudsail.message.MessageType;
import com.cloudsail.transport.ITransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SimpleTcpServerTransport implements ITransport {
    private static final Logger log = Logger.getLogger(SimpleTcpServerTransport.class.getName());
    private int port;
    private int buffSize = 1024;
    public SimpleTcpServerTransport(int port) {
        this.port = port;
    }
    public SimpleTcpServerTransport(int port, int buffSize) {
        this(port);
        this.buffSize = buffSize;
    }
    private List<TransportHandler> handlerList;
    private Map<String, Socket> clientMap = new ConcurrentHashMap<>();
    private ServerSocket server = null;
    private boolean connected = false;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    @Override
    public void open() throws StartFailException {
        try {
            server = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new StartFailException("UDP server start failed!", e);
        }
        this.connected = true;
        executor.execute(() -> {
            while (connected) {
                try {
                    Socket client = server.accept();
                    String address = client.getRemoteSocketAddress().toString().substring(1);
                    clientMap.put(address, client);
                    InputStream inputStream = client.getInputStream();
                    byte[] tmp = new byte[this.buffSize];
                    int len;
                    while ((len = inputStream.read(tmp)) != -1){
                        byte[] buff = new byte[len];
                        System.arraycopy(tmp, 0, buff, 0, len);
                        Message message = new Message();
                        message.setType(MessageType.TCP);
                        message.setBuff(buff);
                        message.setAddress(address);
                        handlerList.forEach(handler -> handler.onDataArrived(this, message));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        log.info("TCP server is listening on [" + this.port + "]...");
    }

    @Override
    public void close() {
        if (server != null) {
            try {
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        server = null;
        connected = false;
        executor.shutdown();
    }

    @Override
    public boolean isConnected() {
        return this.connected && !server.isClosed();
    }

    @Override
    public void send(Message message) throws SendException {
        if (message.getAddress() != null) {
            if (message.getAddress().equals("*")) {
                Iterator<Map.Entry<String, Socket>> iterator = clientMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Socket> entry = iterator.next();
                    try {
                        OutputStream outputStream = entry.getValue().getOutputStream();
                        outputStream.write(message.getBuff());
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Socket client = clientMap.get(message.getAddress());
                if (client != null) {
                    try {
                        OutputStream outputStream = client.getOutputStream();
                        outputStream.write(message.getBuff());
                        outputStream.flush();
                    } catch (Exception e) {
                        throw new SendException("Send fail", e);
                    }
                }
            }
        }
    }

    @Override
    public ITransport addHandler(TransportHandler handler) {
        if (handlerList == null) {
            handlerList = new ArrayList<>();
        }
        handlerList.add(handler);
        return this;
    }
}
