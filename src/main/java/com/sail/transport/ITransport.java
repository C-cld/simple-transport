package com.sail.transport;

import com.sail.exception.DisconnectException;
import com.sail.exception.SendException;
import com.sail.exception.StartFailException;
import com.sail.handler.TransportHandler;
import com.sail.message.Message;
import com.sail.params.ConnParams;

public interface ITransport {
    void open(ConnParams params) throws StartFailException, DisconnectException;
    void close();
    void send(Message message) throws SendException;
    ITransport addHandler(TransportHandler handler);
}
