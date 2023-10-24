package com.sail.handler;

import com.sail.message.Message;
import com.sail.transport.ITransport;

import java.net.Socket;

public interface TransportHandler {
    void onDataArrived(ITransport sender, Message message);
}
