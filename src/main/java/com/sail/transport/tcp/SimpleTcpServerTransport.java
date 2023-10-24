package com.sail.transport.tcp;

import com.sail.exception.SendException;
import com.sail.exception.StartFailException;
import com.sail.handler.TransportHandler;
import com.sail.message.Message;
import com.sail.message.MessageType;
import com.sail.params.ConnParams;
import com.sail.params.TcpParams;
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
import java.util.logging.Logger;

public class SimpleTcpServerTransport implements ITransport {

    private static final Logger log = Logger.getLogger(SimpleTcpServerTransport.class.getName());

    private List<TransportHandler> handlerList;

    private Map<String, Socket> clientMap = new ConcurrentHashMap<>();

    private ServerSocket server = null;

    @Override
    public void open(ConnParams params) throws StartFailException {
        TcpParams tcpParams = (TcpParams) params;
        try {
            server = new ServerSocket(tcpParams.getPort());
        } catch (IOException e) {
            throw new StartFailException("UDP server start failed!", e);
        }
        log.info("TCP server is listening on [" + tcpParams.getPort() + "]...");
        while (true) {
            try {
                Socket client = server.accept();
                String address = client.getRemoteSocketAddress().toString().substring(1);
                clientMap.put(address, client);
                InputStream inputStream = client.getInputStream();
                byte[] tmp = new byte[tcpParams.getBuffSize()];
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
    }

    @Override
    public void close() {

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
    public void addHandler(TransportHandler handler) {
        if (handlerList == null) {
            handlerList = new ArrayList<>();
        }
        handlerList.add(handler);
    }
}
