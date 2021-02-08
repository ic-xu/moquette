package pushmanager.mvc;

import lombok.Data;

@Data
public class ServerInfo {

    private String clientId;

    private String url;

    private int port;

    private int sslPort;
}


