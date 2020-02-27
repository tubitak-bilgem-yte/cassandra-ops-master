package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class RestoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "BACKUP_NAME")
    private String backupName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "UNIQUE_BACKUP_NAMES", joinColumns = @JoinColumn(name = "unique_backup_id"))
    private List<String> uniqueBackupNames;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "BACKUP_PATHS", joinColumns = @JoinColumn(name = "backup_paths_id"))
    private Set<String> backupPaths;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "RESTORE_KEYSPACES", joinColumns = @JoinColumn(name = "restore_keyspaces_id"))
    private Set<String> restoreKeyspaces;

    @Column(name = "RESTORE_NODE_COUNT")
    private Short restoreNodeCount;

    @Column(name = "PARENT_RELATION")
    private String parentRelation;

    public RestoreEntity(final String backupName, final List<String> uniqueBackupNames, final Set<String> backupPaths, final short restoreNodeCount, final String parentRelation, final Set<String> restoreKeypaces) {
        this.backupName = backupName;
        this.uniqueBackupNames = uniqueBackupNames;
        this.backupPaths = backupPaths;
        this.restoreNodeCount = restoreNodeCount;
        this.parentRelation = parentRelation;
        this.restoreKeyspaces = restoreKeypaces;
    }


}
