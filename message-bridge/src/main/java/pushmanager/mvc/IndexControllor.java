package pushmanager.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pushmanager.comment.BaseResponseDto;
import pushmanager.core.MqttClientWrapping;
import pushmanager.mvc.service.BalanceService;
import pushmanager.utils.IdWorker;


@RestController
public class IndexControllor {

    @Autowired
    BalanceService balanceService;


    @GetMapping("/server")
    public BaseResponseDto getServerIp() {

        MqttClientWrapping mqttClientWrapping = balanceService.selectOne();
        if (null == mqttClientWrapping) {
            return BaseResponseDto.error("暂时没有找到任何可用的服务器");
        }
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setUrl(mqttClientWrapping.getClientInfo().getOutIp());
        serverInfo.setPort(mqttClientWrapping.getClientInfo().getPort());
        serverInfo.setSslPort(mqttClientWrapping.getClientInfo().getSslPort());
        serverInfo.setClientId(IdWorker.getInstance().getRandomString(5) + IdWorker.getInstance().nextId());
        return BaseResponseDto.success(serverInfo);
    }
}
