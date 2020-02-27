package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api.BackupCoordinator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.entity.BackupType;


@RestController
public class BackupController {
    private BackupCoordinator backupCoordinator;

    @GetMapping("/{cluster}/snapshot")
    public String requestSnapshot(@PathVariable final String cluster) {
        return backupCoordinator.requestBackup(BackupType.SNAPSHOT, cluster);
    }

    @GetMapping("/{cluster}/incrementalBackup")
    public String requestIncrementalBackup(@PathVariable final String cluster) {
        return backupCoordinator.requestBackup(BackupType.INCREMENTAL_BACKUP, cluster);
    }

    @Autowired
    protected void setBackupCoordinator(final BackupCoordinator backupCoordinator) {
        this.backupCoordinator = backupCoordinator;
    }
}
