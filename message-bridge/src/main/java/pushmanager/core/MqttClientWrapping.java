package pushmanager.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import pushmanager.core.dispatch.MessageDispatch;
import pushmanager.core.dispatch.MessageWrapping;
import pushmanager.core.mode.ClientInfo;
import pushmanager.utils.IPStringUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j(topic = "MqttClientWrapping")
public class MqttClientWrapping implements Runnable {

    private long lastTime;
    private String url;
    private String clientId;
    private String userName;
    private String password;
    private boolean cleanSession;
    private int keepAliveTime;
    private ClientInfo clientInfo;
    private org.eclipse.paho.client.mqttv3.MqttClient mqttClient;
    int[] Qos = {2, 2};
    String[] registerTopics = {"$SYS_METRICS", "$SYS_BROADCASTING"};

    List<String> registerTopicsArrays = Arrays.asList(registerTopics);

    private Map<String, MqttMessage> messageQueue = new ConcurrentHashMap<>();

    public MqttClientWrapping(String ipPort, String userName, String password, boolean cleanSession, int keepAliveTime, ClientInfo clientInfo) {
        this.url = ipPort;
        this.clientId = Constant.clientId;
        this.userName = userName;
        this.password = password;
        this.cleanSession = cleanSession;
        this.keepAliveTime = keepAliveTime;
        this.clientInfo = clientInfo;
    }

    public long getLastTime() {
        return lastTime;
    }


    public String getUrl() {
        return url;
    }

    private void initClient() {
        try {
            lastTime = 0;
            mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(url, clientId);
        } catch (MqttException e) {
            log.error(e.getMessage());
            mqttClient = null;
        }
    }


    // 连接MQTT服务器
    public void startClient() {
        initClient();
        if (mqttClient == null) {
            log.info("mqttClient is null");
            initClient();
        }
        if (mqttClient.isConnected()) {
            close();
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(5); // 设置超时时间
        options.setCleanSession(cleanSession);
        options.setKeepAliveInterval(keepAliveTime);// 设置会话心跳时间
        options.setAutomaticReconnect(true); // 自动重连
        try {
            mqttClient.setCallback(new BtcMqttCallback());
            mqttClient.connect(options);
            subscribe();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("startClient() isConnected:" + mqttClient.isConnected());
    }


    private void close() {
        if (mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close(true);
            } catch (MqttException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void publish(String topic, MqttMessage mqttMessage) throws MqttException {
        mqttClient.publish(topic, mqttMessage);
    }

    // 订阅主题
    private void subscribe() {
        try {
            mqttClient.subscribe(registerTopics, Qos);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    public org.eclipse.paho.client.mqttv3.MqttClient getMqttClient() {
        return mqttClient;
    }

    @Override
    public void run() {
        startClient();
    }

    private class BtcMqttCallback implements MqttCallbackExtended {

        public void connectionLost(Throwable cause) {
            log.info(url + ":  connection lost");
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            log.info("delivery Complete:" + token.isComplete());
        }

        public void messageArrived(String topic, MqttMessage message) {
            lastTime = System.currentTimeMillis();
            String msg = new String(message.getPayload(), Charset.forName("UTF-8"));
            log.info("messageArrived() topic:" + topic);
            log.info(msg);
            if (!registerTopicsArrays.contains(topic)) {
                MessageDispatch.getInstance().addMessage(
                        new MessageWrapping(IPStringUtils.getUrl(clientInfo.getOutIp(),
                                clientInfo.getPort()),
                                topic, message));
            } else {
                messageQueue.put(topic, message);
            }
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            log.info("connectComplete() reconnect:" + reconnect + " serverURI:" + serverURI);
            lastTime = System.currentTimeMillis();
            subscribe();
        }
    }

}
