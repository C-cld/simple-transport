package com.cloudsail;

public class MqttParams {
    private String clientId;
    private String address;
    private String username;
    private String password;
    private String[] producerTopic;
    private String[] consumerTopic;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getProducerTopic() {
        return producerTopic;
    }

    public void setProducerTopic(String[] producerTopic) {
        this.producerTopic = producerTopic;
    }

    public String[] getConsumerTopic() {
        return consumerTopic;
    }

    public void setConsumerTopic(String[] consumerTopic) {
        this.consumerTopic = consumerTopic;
    }
}
