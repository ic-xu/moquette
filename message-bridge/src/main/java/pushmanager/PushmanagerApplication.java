package pushmanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pushmanager.core.MqttConnectManager;


@SpringBootApplication
public class PushmanagerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PushmanagerApplication.class, args);
    }

    @Override
    public void run(String... args) {
//        ClientInfo clientInfo = new ClientInfo(0,"127.0.0.1","127.0.0.1",1883);
        MqttConnectManager.getInstance();
    }
}
