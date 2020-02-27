package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestoreDTO {

    private String clusterName;
    private String backupName;
    private String nodeName;
    private String batchRelation;
    private String parentRelation;
    private List<String> batchBackupFolders;
    private List<String> allBackupFolders;
    private List<String> restoreKeyspaces;
}
