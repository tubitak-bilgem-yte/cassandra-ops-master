package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;

public interface BackupRequester {
    CommandResult requestBackup(NodeEntity nodeEntity, BackupArgs backupArgs);
}
