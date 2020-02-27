package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;

import java.util.List;

public interface BackupRelationRequester {
    List<String> requestBackupRelations(NodeEntity nodeEntity, String path);
    List<String> requestBackupDirectories(NodeEntity nodeEntity, String path);
}
