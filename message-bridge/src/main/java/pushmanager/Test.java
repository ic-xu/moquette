package pushmanager;

import org.eclipse.paho.client.mqttv3.*;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;

public class Test {



    public static void main(String[] args) throws MqttException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException {

        String[] topics = {"test"};
        int[] qos = {2};
        SSLSocketFactory ssf = new Test().configureSSLSocketFactory("/local/sda/project/nim/nim-server/src/main/resources/static/ssl/clientStore.jks");
        MqttClient mqttClientWrapping = new MqttClient("ssl://localhost:8883","testuser");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(ssf);
        options.setUserName("testuser");
        options.setPassword("passwd".toCharArray());
        options.setConnectionTimeout(5); // 设置超时时间
        options.setCleanSession(false);
        options.setKeepAliveInterval(60);// 设置会话心跳时间
        options.setAutomaticReconnect(true); // 自动重连
        mqttClientWrapping.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                try {
                    mqttClientWrapping.subscribe(topics,qos);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                System.err.println("s  :  "+s);
                System.err.println(new String(mqttMessage.getPayload(), StandardCharsets.UTF_8));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }

        });
        mqttClientWrapping.connect(options);

    }



    private SSLSocketFactory configureSSLSocketFactory(String keystore) throws KeyManagementException,
            NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream jksInputStream = new FileInputStream(new File(keystore));
        ks.load(jksInputStream, "nim-chen@1994".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "nim-chen@1994".toCharArray());



        KeyStore ks1 = KeyStore.getInstance("JKS");
        InputStream jksInputStream1 = new FileInputStream(new File(keystore));
        ks1.load(jksInputStream1, "nim-chen@1994".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks1);

        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);

        SSLSocketFactory ssf = sc.getSocketFactory();
        return ssf;
    }
}
