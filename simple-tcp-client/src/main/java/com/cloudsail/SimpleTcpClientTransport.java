package com.cloudsail;

import com.cloudsail.exception.DisconnectException;
import com.cloudsail.exception.SendException;
import com.cloudsail.exception.StartFailException;
import com.cloudsail.handler.TransportHandler;
import com.cloudsail.message.Message;
import com.cloudsail.message.MessageType;
import com.cloudsail.transport.ITransport;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SimpleTcpClientTransport implements ITransport {
    private static final Logger log = Logger.getLogger(SimpleTcpClientTransport.class.getName());
    private String ip = "0.0.0.0";
    private int port;
    private int buffSize = 1024;
    public SimpleTcpClientTransport(int port) {
        this.port = port;
    }
    public SimpleTcpClientTransport(String ip, int port) {
        this(port);
        this.ip = ip;
    }
    public SimpleTcpClientTransport(String ip, int port, int buffSize) {
        this(ip, port);
        this.buffSize = buffSize;
    }
    private List<TransportHandler> handlerList;
    private Socket client = null;
    private boolean connected = false;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    @Override
    public void open() throws StartFailException, DisconnectException {
        try {
            client = new Socket(ip, port);
        } catch (Exception e) {
            throw new StartFailException("TCP client start failed!", e);
        }
        this.connected = true;
        executor.execute(() -> {
            while (connected) {
                try {
                    InputStream inputStream = client.getInputStream();
                    byte[] buff = new byte[buffSize];
                    int length = 0;
                    while ((length = inputStream.read(buff)) != -1) {
                        byte[] data = new byte[length];
                        System.arraycopy(buff, 0, data, 0, length);
                        Message message = new Message();
                        message.setType(MessageType.TCP);
                        message.setBuff(buff);
                        handlerList.forEach(handler -> handler.onDataArrived(this, message));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void send(Message message) throws SendException {
        try {
            OutputStream outputStream = client.getOutputStream();
            outputStream.write(message.getBuff());
            outputStream.flush();
        } catch (Exception e) {
            throw new SendException("Send fail", e);
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
