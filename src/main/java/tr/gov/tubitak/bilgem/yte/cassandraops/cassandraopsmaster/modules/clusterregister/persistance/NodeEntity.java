package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"CLUSTER_NAME", "nodeName"}),
        @UniqueConstraint(columnNames = {"ip", "port"})
})
@Entity
@EqualsAndHashCode(exclude = {"id"})
@Getter
@Setter
public class NodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "CLUSTER_NAME")
    private String clusterName;
    private String nodeName;
    private String tempBackupPath;
    private String ip;
    private Integer port;

    public String checkAllFieldsNotNull() {
        List<String> nullFields = new ArrayList<>();
        String error_null_template = "The following fields cannot be null: %s";
        String message;

        if (this.nodeName == null) {
            nullFields.add("nodeName");
        }
        if (this.tempBackupPath == null) {
            nullFields.add("tempBackupPath");
        }
        if (this.ip == null) {
            nullFields.add("ip");
        }
        if (this.port == null) {
            nullFields.add("port");
        }

        if (nullFields.size() == 0) {
            message = "OK";
        } else {
            message = String.format(error_null_template, String.join(",", nullFields));
        }

        return message;
    }

}
