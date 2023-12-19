package com.cloudsail.transport;

import com.cloudsail.exception.DisconnectException;
import com.cloudsail.exception.SendException;
import com.cloudsail.exception.StartFailException;
import com.cloudsail.handler.TransportHandler;
import com.cloudsail.message.Message;

public interface ITransport {
    void open() throws StartFailException, DisconnectException;
    void close();
    boolean isConnected();
    void send(Message message) throws SendException;
    ITransport addHandler(TransportHandler handler);
}
