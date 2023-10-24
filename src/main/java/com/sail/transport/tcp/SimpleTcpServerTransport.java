package com.sail.transport.tcp;

import com.sail.exception.SendException;
import com.sail.exception.StartFailException;
import com.sail.handler.TransportHandler;
import com.sail.message.Message;
import com.sail.message.MessageType;
import com.sail.transport.ITransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    private boolean flag = true;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    @Override
    public void open() throws StartFailException {
        try {
            server = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new StartFailException("UDP server start failed!", e);
        }
        executor.execute(() -> {
            while (flag) {
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
        flag = false;
        executor.shutdown();
    }

    @Override
    public void send(Message message) throws SendException {
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

    @Override
    public ITransport addHandler(TransportHandler handler) {
        if (handlerList == null) {
            handlerList = new ArrayList<>();
        }
        handlerList.add(handler);
        return this;
    }
}
