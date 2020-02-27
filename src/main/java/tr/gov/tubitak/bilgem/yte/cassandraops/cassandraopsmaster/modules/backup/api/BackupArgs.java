package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BackupArgs {
    private String cassandraDataPath;
    private String tempBackupPath;
    private String backupLabel;
    private String backupType;
    private List<String> keyspaces;
    private String relation;
    private boolean cleanOldBackups;

}
