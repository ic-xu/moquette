package pushmanager.core;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import pushmanager.core.mode.ClientInfo;
import pushmanager.core.mode.ClientInfoRepository;
import pushmanager.utils.IPStringUtils;
import pushmanager.utils.SpringbootApplicationUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
public class MqttConnectManager {


    private Map<String, MqttClientWrapping> metricsStatusQueue;

    private ScheduledExecutorService connectExecutorService;

    private ScheduledExecutorService clearTimeoutExecutorService;

    private int wittingTime = 160;

    private static volatile MqttConnectManager instance;

    private long timeout = 60000;


    private MqttConnectManager() {
        connectExecutorService = Executors.newScheduledThreadPool(1);
        clearTimeoutExecutorService = Executors.newScheduledThreadPool(1);
        metricsStatusQueue = new ConcurrentHashMap<>();
        connectExecutorService.scheduleWithFixedDelay(new ConnectWork(), 0, wittingTime, TimeUnit.SECONDS);
        clearTimeoutExecutorService.scheduleWithFixedDelay(new CleanWorker(),0,wittingTime,TimeUnit.SECONDS);
    }


    public static MqttConnectManager getInstance() {
        if (null == instance) {
            synchronized (MqttConnectManager.class) {
                if (null == instance) {
                    instance = new MqttConnectManager();
                }
            }
        }
        return instance;
    }


    public Map<String, MqttClientWrapping> getMetricsStatusQueue() {
        return metricsStatusQueue;
    }

    class ConnectWork implements Runnable {
        @Override
        public void run() {
            ClientInfoRepository clientInfoRepository = SpringbootApplicationUtils.getBean(ClientInfoRepository.class);
            List<ClientInfo> clientInfos = clientInfoRepository.findAll();
            for (ClientInfo clientInfo : clientInfos) {
                if (null != clientInfo && !metricsStatusQueue.containsKey(IPStringUtils.getUrl(clientInfo.getOutIp(), clientInfo.getPort()))) {
                    MqttClientWrapping mqttClientWrapping = new MqttClientWrapping(
                            IPStringUtils.getUrl(clientInfo.getInnerIp(), clientInfo.getPort()),
                            clientInfo.getUsername(),
                            clientInfo.getPassword(),
                            false,
                            Constant.keepAliveTime, clientInfo);
                    mqttClientWrapping.startClient();
                    metricsStatusQueue.put(IPStringUtils.getUrl(clientInfo.getOutIp(), clientInfo.getPort()), mqttClientWrapping);
                }
            }
        }
    }



    class CleanWorker implements Runnable {
        @Override
        public void run() {
            for (String nextKey : metricsStatusQueue.keySet()) {
                MqttClientWrapping mqttClientWrapping = metricsStatusQueue.get(nextKey);
                if (System.currentTimeMillis() - mqttClientWrapping.getLastTime() > timeout
                        && !mqttClientWrapping.getMqttClient().isConnected()) {
                    try {
                        mqttClientWrapping.getMqttClient().disconnectForcibly(3000);
                    } catch (MqttException e) {
                        log.error(e.getMessage());
                    }
                    metricsStatusQueue.remove(nextKey);
                }
            }

        }
    }
}
