package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api.entity.CopyArgs;

public interface CopyBackupRequester {
    CommandResult requestCopyFromNode(NodeEntity nodeEntity, CopyArgs copyArgs);
}
