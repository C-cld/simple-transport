package com.sail.transport.tcp;

import com.sail.exception.DisconnectException;
import com.sail.exception.SendException;
import com.sail.exception.StartFailException;
import com.sail.handler.TransportHandler;
import com.sail.message.Message;
import com.sail.message.MessageType;
import com.sail.transport.ITransport;

import java.io.InputStream;
import java.net.Socket;
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

    }

    @Override
    public ITransport addHandler(TransportHandler handler) {
        return null;
    }
}
