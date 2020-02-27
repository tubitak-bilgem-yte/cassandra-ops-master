package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.PingStatus;

public interface PingRequester {
    PingStatus pingNode(NodeEntity nodeEntity);
}
