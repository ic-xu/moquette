package pushmanager.utils;

public class IPStringUtils {

    public static String getUrl(String ip, int port){
        return "tcp://" + ip+ ":" + port;
    }
}
