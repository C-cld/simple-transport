package com.cloudsail;

import com.cloudsail.exception.DisconnectException;
import com.cloudsail.exception.SendException;
import com.cloudsail.exception.StartFailException;
import com.cloudsail.handler.TransportHandler;
import com.cloudsail.message.Message;
import com.cloudsail.message.MessageType;
import com.cloudsail.transport.ITransport;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SimpleMqttTransport implements ITransport {
    private static final Logger log = Logger.getLogger(SimpleMqttTransport.class.getName());
    private MqttParams connParams = null;
    public SimpleMqttTransport(MqttParams connParams) {
        this.connParams = connParams;
    }

    private MqttClient mqttClient;
    private List<TransportHandler> handlerList;
    private boolean connected = false;
    @Override
    public void open() throws StartFailException, DisconnectException {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(connParams.getUsername());
        options.setPassword(connParams.getPassword().toCharArray());
        options.setConnectionTimeout(30);///默认：30
        options.setAutomaticReconnect(true);//默认：false
        options.setCleanSession(true);//默认：true

        try {
            mqttClient = new MqttClient(connParams.getAddress(), connParams.getClientId(), new MemoryPersistence());
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    connected = false;
                    log.info("MQTT transport start fail");
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    try {
                        log.info("Recv: " + topic + " - " + mqttMessage);
                        String payload = new String(mqttMessage.getPayload()).replaceAll("\r\n", "");
                        if (handlerList != null) {
                            Message message = new Message();
                            message.setAddress(topic);
                            message.setBuff(payload.getBytes(Charset.forName("UTF-8")));
                            message.setType(MessageType.MQTT);
                            handlerList.forEach(handler -> {handler.onDataArrived(null, message);});
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("Send success");
                }
            });

            mqttClient.connect(options);
            mqttClient.subscribe(connParams.getConsumerTopic());
            log.info("MQTT connected with client id [" + connParams.getClientId() + "]...");
            connected = true;
        } catch (MqttException e) {
            throw new StartFailException("MQTT transport start fail", e);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isConnected() {
        return mqttClient.isConnected() && this.connected;
    }

    @Override
    public void send(Message message) throws SendException {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(2);
            mqttMessage.setPayload(message.getBuff());
            if (message.getAddress() == null || message.getAddress().equals("")) {
                for (String topic : connParams.getProducerTopic()) {
                    MqttTopic mqttTopic = mqttClient.getTopic(topic);
                    MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
                    token.waitForCompletion();
                    log.info("Send: " + topic + " - " + mqttMessage);
                }
            } else {
                MqttTopic mqttTopic = mqttClient.getTopic(message.getAddress());
                MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
                token.waitForCompletion();
                log.info("Send: " + message.getAddress() + " - " + mqttMessage);
            }

        } catch (Exception e) {
            throw new SendException("Send fail", e);
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
