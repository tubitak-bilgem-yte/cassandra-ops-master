package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommandResultRepository extends CrudRepository<CommandResult, Long> {
    List<CommandResult> findByRelation(String relation);

    CommandResult findByRelationAndClusterNameAndNodeName(String relation,
                                                          String clusterName,
                                                          String nodeName);
}
