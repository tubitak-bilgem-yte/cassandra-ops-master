package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"relation", "clusterName", "nodeName"})
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CommandResult {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String commandName;
    private String relation;
    private String status;
    private String message;
    private String clusterName;
    private String nodeName;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value", columnDefinition = "text")
    @CollectionTable(name = "command_result_more_info", joinColumns = @JoinColumn(name = "command_result_id"))
    private Map<String, String> moreInfo;

    public CommandResult(final String commandName,
                         final String relation,
                         final String status,
                         final String message,
                         final String clusterName,
                         final String nodeName,
                         final Map<String, String> moreInfo) {
        setCommandName(commandName);
        setRelation(relation);
        setStatus(status);
        setMessage(message);
        setClusterName(clusterName);
        setNodeName(nodeName);
        setMoreInfo(moreInfo);
    }

    public Map<String, String> getMoreInfo() {
        if (moreInfo == null) {
            moreInfo = new HashMap<>();
        }
        return moreInfo;
    }
}
