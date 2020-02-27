package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@EqualsAndHashCode
public class NodeDTO {

    @JsonProperty(value = "nodeName")
    private String nodeName;

    @JsonProperty(value = "tempBackupPath")
    private String tempBackupPath;

    @JsonProperty(value = "ip")
    @Pattern(regexp = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$", message = "Invalid IP address!")
    private String ip;
    @JsonProperty(value = "port")
    private Integer port;
}
