package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.PingStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PingService {
    private final NodeEntityRepository nodeEntityRepository;
    private final PingRequester pingRequester;

    /*
        Sends a ping request to each registered node to find which nodes are down and which ones are up,
        and returns it as a {ip:status} map
     */
    public Map<String, PingStatus> pingNodes(final String clusterName) {
        Map<String, PingStatus> agentStatus = new LinkedHashMap<>();
        for (NodeEntity agent : nodeEntityRepository.findByClusterName(clusterName)) {
            agentStatus.put(agent.getIp(), pingRequester.pingNode(agent));
        }
        return agentStatus;
    }

    /*
		   Sends a ping request to all nodes, and depending on their responses, it find unhealthy nodes and return
		   their IP addresses.
		*/
    public List<String> findUnhealthyNodes(final String clustername) {
        Map<String, PingStatus> pingStatuses = pingNodes(clustername);
        return pingStatuses.entrySet().stream()
                .filter(e -> e.getValue().equals(PingStatus.DOWN))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
