package com.sail.params;

public class TcpParams extends ConnParams {
    private int port = -1;

    private int buffSize = 1024;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBuffSize() {
        return buffSize;
    }

    public void setBuffSize(int buffSize) {
        this.buffSize = buffSize;
    }
}
