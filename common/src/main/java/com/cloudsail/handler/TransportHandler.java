package com.cloudsail.handler;

import com.cloudsail.message.Message;
import com.cloudsail.transport.ITransport;

public interface TransportHandler {
    void onDataArrived(ITransport sender, Message message);
}
