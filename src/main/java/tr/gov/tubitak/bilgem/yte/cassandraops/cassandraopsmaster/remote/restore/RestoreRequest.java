package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestoreRequest {
    String clusterName;
    String backupName;
    List<String> restoreKeyspaces;
}
