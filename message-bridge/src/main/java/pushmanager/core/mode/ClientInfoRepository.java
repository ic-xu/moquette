package pushmanager.core.mode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientInfoRepository extends JpaRepository<ClientInfo,Integer> {

}
