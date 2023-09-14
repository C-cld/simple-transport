package com.sail.transport.mqtt;

import com.sail.exception.DisconnectException;
import com.sail.exception.SendException;
import com.sail.exception.StartFailException;
import com.sail.handler.TransportHandler;
import com.sail.message.Message;
import com.sail.message.MessageType;
import com.sail.params.ConnParams;
import com.sail.params.MqttParams;
import com.sail.transport.ITransport;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MqttTransport implements ITransport {
    private static final Logger log = Logger.getLogger(MqttTransport.class.getName());
    private MqttClient mqttClient;
    private List<TransportHandler> handlerList;

    private boolean connected = false;

    private MqttParams connParams = null;

    @Override
    public void open(ConnParams params) throws StartFailException, DisconnectException {
        MqttParams mqttParams = (MqttParams) params;
        connParams = mqttParams;

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqttParams.getUsername());
        options.setPassword(mqttParams.getPassword().toCharArray());
        options.setConnectionTimeout(30);///默认：30
        options.setAutomaticReconnect(true);//默认：false
        options.setCleanSession(true);//默认：true

        try {
            mqttClient = new MqttClient(mqttParams.getAddress(), mqttParams.getClientId(), new MemoryPersistence());
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    connected = false;
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
            mqttClient.subscribe(mqttParams.getConsumerTopic());
            log.info("MQTT connected...");
            connected = true;
        } catch (MqttException e) {
            throw new StartFailException("MQTT transport start fail", e);
        }

        while (true) {
            if (!connected) {
                throw new DisconnectException("MQTT disconnect!");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
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
    public void addHandler(TransportHandler handler) {
        if (handlerList == null) {
            handlerList = new ArrayList<>();
        }
        handlerList.add(handler);
    }
}
