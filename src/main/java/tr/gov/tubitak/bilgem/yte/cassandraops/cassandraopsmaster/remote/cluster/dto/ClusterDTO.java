package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class ClusterDTO {

    @NotNull(message = "Keyspaces cannot be null")
    @JsonProperty(value = "keyspaces")
    List<String> keyspaces;
    @NotNull(message = "Cluster name cannot be null!")
    @JsonProperty(value = "clusterName")
    private String clusterName;
    @NotNull(message = "Backup path cannot be null!")
    @JsonProperty(value = "backupPath")
    private String backupPath;
    @JsonProperty(value = "scheduleInfo")
    @Valid
    private ScheduleDTO schedule;

    @JsonProperty(value = "nodes")
    @Valid
    private List<NodeDTO> nodes;

}
