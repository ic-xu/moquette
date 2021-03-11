package pushmanager.core.mode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;

@Slf4j
@Data
@Entity(name = "client")
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo {

    @Id
    private Integer id = 0;

    private String outIp = "10.92.33.61";

    private String innerIp = "10.92.33.61";

    private int port=1883;

    private int sslPort=8883;

    private String password="admin";

    private String username="passwd";


    /*
     *
     *
     *  push_register01@#0LZ
     *
 CREATE TABLE `client` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inner_ip` varchar(255) DEFAULT NULL,
  `out_ip` varchar(255) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `ssl_port` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8
insert into client(out_ip,inner_ip,port,ssl_port) values("52.82.57.237","52.82.57.237",1883,8883);
     *
     *
     *
     */
}
