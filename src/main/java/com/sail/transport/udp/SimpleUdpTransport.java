package com.sail.transport.udp;

import com.sail.exception.SendException;
import com.sail.exception.StartFailException;
import com.sail.handler.TransportHandler;
import com.sail.message.Message;
import com.sail.message.MessageType;
import com.sail.params.ConnParams;
import com.sail.params.UdpParams;
import com.sail.transport.ITransport;
import com.sail.util.FrameUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SimpleUdpTransport implements ITransport {
    private static final Logger log = Logger.getLogger(SimpleUdpTransport.class.getName());
    private DatagramSocket server = null;
    private List<TransportHandler> handlerList;

    @Override
    public void open(ConnParams params) throws StartFailException {
        UdpParams udpParams = (UdpParams) params;
        try {
            server = new DatagramSocket(udpParams.getPort());
        } catch (SocketException e) {
            throw new StartFailException("UDP Server start fail!", e);
        }
        byte[] buff = new byte[udpParams.getBuffSize()];
        log.info("UDP server is listening on [" + udpParams.getPort() + "]...");
        DatagramPacket packet =new DatagramPacket(buff, buff.length);
        while (true) {
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
    }

    @Override
    public void close() {

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
    public void addHandler(TransportHandler handler) {
        if (handlerList == null) {
            handlerList = new ArrayList<>();
        }
        handlerList.add(handler);
    }
}
