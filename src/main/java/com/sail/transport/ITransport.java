package com.sail.transport;

import com.sail.exception.DisconnectException;
import com.sail.exception.SendException;
import com.sail.exception.StartFailException;
import com.sail.handler.TransportHandler;
import com.sail.message.Message;

public interface ITransport {
    void open() throws StartFailException, DisconnectException;
    void close();
    boolean isConnected();
    void send(Message message) throws SendException;
    ITransport addHandler(TransportHandler handler);
}
