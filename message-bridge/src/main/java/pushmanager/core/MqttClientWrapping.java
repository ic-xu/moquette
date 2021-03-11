package pushmanager.core;

import io.client.mqttv3.*;
import lombok.extern.slf4j.Slf4j;
import pushmanager.core.dispatch.MessageDispatch;
import pushmanager.core.dispatch.MessageWrapping;
import pushmanager.core.mode.ClientInfo;
import pushmanager.utils.IPStringUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "MqttClientWrapping")
public class MqttClientWrapping extends MqttClient implements Runnable {

    private long lastTime;
    private String userName;
    private String password;
    private boolean cleanSession;
    private int keepAliveTime;
    private ClientInfo clientInfo;





    int[] Qos = {2, 2};
    String[] registerTopics = {"$SYS_METRICS", "$SYS_BROADCASTING"};

    List<String> registerTopicsArrays = Arrays.asList(registerTopics);

    private Map<String, MqttMessage> messageQueue = new ConcurrentHashMap<>();

    public MqttClientWrapping(String ipPort, String userName, String password, boolean cleanSession, int keepAliveTime, ClientInfo clientInfo) throws MqttException {
        super(ipPort, Constant.clientId);
        this.userName = userName;
        this.password = password;
        this.cleanSession = cleanSession;
        this.keepAliveTime = keepAliveTime;
        this.clientInfo = clientInfo;
    }

    public long getLastTime() {
        return lastTime;
    }


    public void gettetst(){
    }


    // 连接MQTT服务器
    public void startClient() {
        try {
            if (isConnected()) {
                close();
            }
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(userName);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(5); // 设置超时时间
            options.setCleanSession(cleanSession);
            options.setKeepAliveInterval(keepAliveTime);// 设置会话心跳时间
            options.setAutomaticReconnect(true); // 自动重连
            setCallback(new BtcMqttCallback());
            connect(options);
            subscribe();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("startClient() isConnected:" + isConnected());
    }


    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void publish(String topic, MqttMessage mqttMessage) throws MqttException {
        publish(topic, mqttMessage);
    }

    // 订阅主题
    private void subscribe() {
        try {
            subscribe(registerTopics, Qos);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    public MqttClient getMqttClient() {
        return this;
    }

    @Override
    public void run() {
        startClient();
    }

    public class BtcMqttCallback implements MqttCallbackExtended {

        public void connectionLost(Throwable cause) {
            log.info(getServerURI() + ":  connection lost");
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
