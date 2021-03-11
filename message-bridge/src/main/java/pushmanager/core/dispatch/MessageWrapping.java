package pushmanager.core.dispatch;

import io.client.mqttv3.MqttMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageWrapping {

    private String sourceUrl;

    private String topic;

    private MqttMessage mqttMessage;
}
