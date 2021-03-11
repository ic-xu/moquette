package io.handler.codec.mqtt;


/**
 * Variable header of {@link MqttConnectMessage}
 */
public final class MqttCustomerVariableHeader {
    private short packageId;


    public short getPackageId() {
        return packageId;
    }


    public void setPackageId(short packageId) {
        this.packageId = packageId;
    }

    public MqttCustomerVariableHeader(Short packageId){

        this.packageId = packageId;
    }
}
