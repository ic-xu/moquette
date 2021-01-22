//package io.moquette.Interceptor;
//
//import io.moquette.broker.MQTTConnection;
//import io.netty.handler.codec.mqtt.MqttMessage;
//
//import java.util.List;
//
//public class MessageManagerTask implements Runnable {
//
//    List<Interceptor> customInterceptor;
//    MQTTConnection mqttConnection;
//    MqttMessage msg;
//
//    private MessageManagerTask() {
//    }
//
//
//    public MessageManagerTask(List<Interceptor> customInterceptor,
//                              MQTTConnection mqttConnection,
//                              MqttMessage msg) {
//        this.customInterceptor = customInterceptor;
//        this.mqttConnection = mqttConnection;
//        this.msg = msg;
//
//    }
//
//    @Override
//    public void run() {
//        InterceptorChain interceptorChain = new InterceptorChain();
//        interceptorChain.addInterceptor(new ExpresstionInterceptor());
//        if (null != customInterceptor)
//            interceptorChain.addAllInterceptor(customInterceptor);
//        interceptorChain.addInterceptor(new LastMessageInterceotor());
//        interceptorChain.doProcess(mqttConnection, msg, interceptorChain);
//
//        customInterceptor = null;
//        mqttConnection = null;
//        msg = null;
//    }
//
//
//}
