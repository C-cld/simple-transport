package com.cloudsail;

import com.cloudsail.exception.SendException;
import com.cloudsail.exception.StartFailException;
import com.cloudsail.handler.TransportHandler;
import com.cloudsail.message.Message;
import com.cloudsail.message.MessageType;
import com.cloudsail.transport.ITransport;
import com.cloudsail.util.FrameUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SimpleUdpTransport implements ITransport {
    private static final Logger log = Logger.getLogger(SimpleUdpTransport.class.getName());
    private int port;
    private int buffSize = 1024;
    public SimpleUdpTransport(int port) {
        this.port = port;
    }
    public SimpleUdpTransport(int port, int buffSize) {
        this(port);
        this.buffSize = buffSize;
    }

    private DatagramSocket server = null;
    private boolean connected = false;
    private List<TransportHandler> handlerList;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    @Override
    public void open() throws StartFailException {
        try {
            server = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new StartFailException("UDP Server start failed!", e);
        }
        this.connected = true;
        executor.execute(() -> {
            byte[] buff = new byte[buffSize];
            DatagramPacket packet =new DatagramPacket(buff, buff.length);
            while (connected) {
                try {
                    server.receive(packet);
                    int len = packet.getLength();
                    if (len > 0) {
                        byte[] content = new byte[len];
                        System.arraycopy(packet.getData(), 0, content, 0, len);
                        InetSocketAddress socketAddress = (InetSocketAddress) packet.getSocketAddress();
                        String address = String.format("%s:%d", socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
                        log.info("Recv: " + FrameUtil.byteArr2HexString(content) + "(" + address + ")");
                        if (handlerList != null && handlerList.size() > 0) {
                            Message message = new Message();
                            message.setAddress(address);
                            message.setBuff(content);
                            message.setType(MessageType.UDP);
                            handlerList.forEach(handler -> handler.onDataArrived(this, message));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        log.info("UDP server is listening on [" + port + "]...");
    }

    @Override
    public void close() {
        connected = false;
        server.close();
        server = null;
        executor.shutdown();
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void send(Message message) throws SendException {
        try {
            byte[] data = message.getBuff();
            String address = message.getAddress();
            if (data == null) {
                return;
            }
            if (address == null || address.trim().equals("")) {
                return;
            }
            DatagramPacket sendPacket = new DatagramPacket(data, data.length);
            String[] sl = address.split(":");
            InetSocketAddress socketAddress = new InetSocketAddress(sl[0], Integer.parseInt(sl[1]));
            sendPacket.setSocketAddress(socketAddress);
            server.send(sendPacket);
            log.info("Send: " + FrameUtil.byteArr2HexString(data) + "(" + address + ")");
        } catch (Exception e) {
            throw new SendException("Send fail!", e);
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
