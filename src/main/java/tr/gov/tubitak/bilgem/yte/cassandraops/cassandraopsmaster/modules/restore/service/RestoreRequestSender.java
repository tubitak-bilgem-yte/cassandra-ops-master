package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto.RestoreDTO;

public interface RestoreRequestSender {
    String sendRestoreRequest(NodeEntity nodeEntity, RestoreDTO restoreDTO);
}
