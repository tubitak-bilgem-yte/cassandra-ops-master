package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "BACKUP_RELATION")
@Getter
@Setter
@NoArgsConstructor
public class BackupSizeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private int id;

    @Column(name = "TOTAL_SIZE")
    private long totalSize;

    @Column(name = "RELATION_NAME")
    private String relationName;

    public BackupSizeEntity(final long totalSize, final String relationName) {
        this.totalSize = totalSize;
        this.relationName = relationName;
    }
}
