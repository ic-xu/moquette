package io.handler.codec.mqtt;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Payload of the {@link MqttSubAckMessage}
 */
public class MqttSubAckPayload {

    private final List<Integer> reasonCodes;

    public MqttSubAckPayload(int... reasonCodes) {
        ObjectUtil.checkNotNull(reasonCodes, "reasonCodes");

        List<Integer> list = new ArrayList<Integer>(reasonCodes.length);
        for (int v: reasonCodes) {
            list.add(v);
        }
        this.reasonCodes = Collections.unmodifiableList(list);
    }

    public MqttSubAckPayload(Iterable<Integer> reasonCodes) {
        ObjectUtil.checkNotNull(reasonCodes, "reasonCodes");
        List<Integer> list = new ArrayList<Integer>();
        for (Integer v: reasonCodes) {
            if (v == null) {
                break;
            }
            list.add(v);
        }
        this.reasonCodes = Collections.unmodifiableList(list);
    }

    public List<Integer> grantedQoSLevels() {
        List<Integer> qosLevels = new ArrayList<Integer>(reasonCodes.size());
        for (int code: reasonCodes) {
            if (code > MqttQoS.EXACTLY_ONCE.value()) {
                qosLevels.add(MqttQoS.FAILURE.value());
            } else {
                qosLevels.add(code);
            }
        }
        return qosLevels;
    }

    public List<Integer> reasonCodes() {
        return reasonCodes;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("reasonCodes=").append(reasonCodes)
            .append(']')
            .toString();
    }
}
