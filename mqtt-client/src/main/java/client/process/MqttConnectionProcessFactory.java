package client.process;

import io.netty.channel.Channel;

public class MqttConnectionProcessFactory {

    public MQTTConnectionProcess createMqttConnectionProcess(Channel channel){
        return new MQTTConnectionProcess(channel);
    }

}
