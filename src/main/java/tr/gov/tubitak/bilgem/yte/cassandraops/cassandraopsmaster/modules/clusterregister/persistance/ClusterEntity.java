package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
@EqualsAndHashCode
@Getter
@Setter
public class ClusterEntity {

    @Id
    private String clusterName;
    private String backupPath;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "SCHEDULE_ID")
    private ScheduleEntity schedule;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "CLUSTER_NAME")
    private List<NodeEntity> nodes;
    private String keyspaces;
}
