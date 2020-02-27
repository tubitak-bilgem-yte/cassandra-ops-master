package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClusterEntityRepository extends CrudRepository<ClusterEntity, String> {

    @Override
    Optional<ClusterEntity> findById(String s);

    ClusterEntity findByClusterName(String clusterName);

    boolean existsByClusterName(String clusterName);
}
