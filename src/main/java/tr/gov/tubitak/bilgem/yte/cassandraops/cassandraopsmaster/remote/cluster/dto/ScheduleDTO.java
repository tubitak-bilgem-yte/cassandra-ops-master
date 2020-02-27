package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.validation.constraint.SnapshotDayValidation;

import javax.validation.constraints.Pattern;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
public class ScheduleDTO {

    @JsonProperty(value = "snapshotDays")
    @SnapshotDayValidation
    private Set<Integer> snapshotDays;
    @JsonProperty(value = "backupHour")
    @Pattern(regexp = "^(2[0-3]|[01]?[0-9]):([0,3][0])$", message = "Wrong hour format! The format must be in 24 hour format,in HH:MM shape, and minutes can be 30 or 00")
    private String backupHour;
    @JsonProperty("isActive")
    private Boolean isActive;
}
