package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore;

import lombok.Getter;
import lombok.Setter;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;

@Getter
@Setter
public class BatchRestoreResponse {
    private String parentRelation;
    private String batchRelation;
    private String currentUniqueBackupName;
    private CommandResult commandResult;
}
