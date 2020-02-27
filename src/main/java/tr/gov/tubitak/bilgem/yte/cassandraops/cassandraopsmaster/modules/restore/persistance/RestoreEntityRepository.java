package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestoreEntityRepository extends CrudRepository<RestoreEntity, Long> {
    RestoreEntity findByBackupName(String backupName);

    RestoreEntity findById(long restoreEntityId);

    RestoreEntity findByParentRelation(String parentRelation);
}
