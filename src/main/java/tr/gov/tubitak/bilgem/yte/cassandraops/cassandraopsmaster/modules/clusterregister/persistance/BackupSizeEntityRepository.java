package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackupSizeEntityRepository extends CrudRepository<BackupSizeEntity, Integer> {

    BackupSizeEntity findByRelationName(String relationName);
}
