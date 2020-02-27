package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeEntityRepository extends CrudRepository<NodeEntity, String> {

    NodeEntity findByClusterNameAndNodeName(String clusterName, String nodeName);

    List<NodeEntity> findByClusterName(String clusterName);
}
