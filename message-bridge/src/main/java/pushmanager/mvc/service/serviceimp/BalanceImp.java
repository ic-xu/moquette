package pushmanager.mvc.service.serviceimp;

import org.springframework.stereotype.Service;
import pushmanager.core.MqttClientWrapping;
import pushmanager.core.MqttConnectManager;
import pushmanager.mvc.service.BalanceService;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service("random")
public class BalanceImp implements BalanceService {


    @Override
    public MqttClientWrapping selectOne() {
        ArrayList<MqttClientWrapping> mqttClientWrappings = new ArrayList<>();
        Map<String, MqttClientWrapping> mqttClientMap = MqttConnectManager
                .getInstance()
                .getMetricsStatusQueue();

        if (mqttClientMap.size() == 0)
            return null;

        for (String key : mqttClientMap.keySet()) {
            if (mqttClientMap.get(key).getMqttClient().isConnected()) {
                mqttClientWrappings.add(mqttClientMap.get(key));
            }
        }
        if (mqttClientWrappings.size() == 0)
            return null;

        int i = ThreadLocalRandom.current().nextInt(mqttClientWrappings.size());
        return mqttClientWrappings.get(i);
    }
}
