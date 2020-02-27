package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


/*
    Used to determine the name of the next backup file. When a snapshot needs to be created, it will
    get the next counter from this table, and create a new backup directory. This counter is incremented
    only when a snapshot is created, and incremental backup doesn't increment this count.
 */

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClusterBackupCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String clusterName;
    @Column
    private int backupCount;

    public ClusterBackupCounter(final String clusterName, final int backupCount) {
        this.clusterName = clusterName;
        this.backupCount = backupCount;
    }

}
