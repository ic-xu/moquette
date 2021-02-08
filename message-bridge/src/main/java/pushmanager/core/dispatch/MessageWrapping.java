package pushmanager.core.dispatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;


@Data
@AllArgsConstructor
public class MessageWrapping {

    private String sourceUrl;

    private String topic;

    private MqttMessage mqttMessage;
}
