package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api;

import org.junit.Test;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.PingStatus;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.PingRequesterClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData.NODEAGENT1_CLUSTER_NAME;

public class PingServiceTest {

	private PingService pingService;
	private PingRequester pingRequester;
	private List<NodeEntity> testData;

	@Test
	public void pingNodes_pingMultipleNodesAllNodesAreUp_success() {
		init();
		when(pingRequester.pingNode(any(NodeEntity.class))).thenReturn(PingStatus.OK);
		Map<String, PingStatus> expectedPingResult = new HashMap<>();
		for (NodeEntity nodeEntity : testData) {
			expectedPingResult.put(nodeEntity.getIp(), PingStatus.OK);
		}
		Map<String, PingStatus> pingResult = pingService.pingNodes(NODEAGENT1_CLUSTER_NAME);
		assertThat(pingResult, equalTo(expectedPingResult));

	}

	@Test
	public void pingNodes_pingMultipleNodesTwoNodesAreDown_fail() {
		init();
		Map<String, PingStatus> expectedPingResult = new HashMap<>();
		int index = 0;
		for (NodeEntity nodeEntity : testData) {
			if (index <= 2) {
				when(pingRequester.pingNode(nodeEntity)).thenReturn(PingStatus.OK);
				expectedPingResult.put(nodeEntity.getIp(), PingStatus.OK);
			} else {
				when(pingRequester.pingNode(nodeEntity)).thenReturn(PingStatus.DOWN);
				expectedPingResult.put(nodeEntity.getIp(), PingStatus.DOWN);
			}
			index++;
		}
		Map<String, PingStatus> pingResult = pingService.pingNodes(NODEAGENT1_CLUSTER_NAME);
		assertThat(pingResult, equalTo(expectedPingResult));
	}

	public void init() {
		NodeEntityRepository nodeEntityRepository = mock(NodeEntityRepository.class);
		pingRequester = mock(PingRequesterClient.class);
		pingService = new PingService(nodeEntityRepository, pingRequester);
		testData = createTestNodeEntitys();
		when(nodeEntityRepository.findByClusterName(NODEAGENT1_CLUSTER_NAME)).thenReturn(testData);

	}

	private List<NodeEntity> createTestNodeEntitys() {
		NodeEntityTestDataCreator testDataCreator = new NodeEntityTestDataCreator();
		List<NodeEntity> testData = testDataCreator
				.addNodeEntity()
				.setClusterName(NODEAGENT1_CLUSTER_NAME)
				.setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
				.setNodeEntityIpAddress(NodeEntityData.NODEAGENT1_IP)
				.setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
				.addNodeEntity()
				.setClusterName(NodeEntityData.NODEAGENT2_CLUSTER_NAME)
				.setNodeName(NodeEntityData.NODEAGENT2_NODE_NAME)
				.setNodeEntityIpAddress(NodeEntityData.NODEAGENT2_IP)
				.setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
				.addNodeEntity()
				.setClusterName(NodeEntityData.NODEAGENT3_CLUSTER_NAME)
				.setNodeName(NodeEntityData.NODEAGENT3_NODE_NAME)
				.setNodeEntityIpAddress(NodeEntityData.NODEAGENT3_IP)
				.setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
				.addNodeEntity()
				.setClusterName(NodeEntityData.NODEAGENT4_CLUSTER_NAME)
				.setNodeName(NodeEntityData.NODEAGENT4_NODE_NAME)
				.setNodeEntityIpAddress(NodeEntityData.NODEAGENT4_IP)
				.setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
				.addNodeEntity()
				.setClusterName(NodeEntityData.NODEAGENT5_CLUSTER_NAME)
				.setNodeName(NodeEntityData.NODEAGENT5_NODE_NAME)
				.setNodeEntityIpAddress(NodeEntityData.NODEAGENT5_IP)
				.setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
				.build();

		return testData;
	}

}

