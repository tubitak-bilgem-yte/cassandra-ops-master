package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.persistence;

import org.springframework.data.repository.CrudRepository;

public interface ClusterBackupCounterRepository extends CrudRepository<ClusterBackupCounter, Long> {
    ClusterBackupCounter findByClusterName(String clusterName);
}
