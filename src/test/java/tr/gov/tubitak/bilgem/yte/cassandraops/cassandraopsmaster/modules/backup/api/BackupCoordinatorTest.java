package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.entity.BackupType;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.EnvironmentUtil;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.*;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.PingStatus;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api.PingRequester;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api.PingService;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData.*;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterTestData.CLUSTER_1;

@RunWith(MockitoJUnitRunner.class)
public class BackupCoordinatorTest {

    private static final String SUCCESS_STATUS = "OK";
    private static final String FAIL_STATUS = "NOT_OK";
    private static final String ERROR_TEMPLATE = "ERROR: Could not connect to these nodes: %s";
    private static final String CASSANDRA_PATH = "data/data";
    private static final String KEYSPACE = "urunhareketleri";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();


    @Mock
    private ClusterEntityRepository clusterEntityRepository;

    @Mock
    private NodeEntityRepository nodeEntityRepository;

    @Mock
    private CommandResultRepository commandResultRepository;

    @Mock
    private ScheduleEntityRepository scheduleEntityRepository;

    @Mock
    private PingService pingService;

    @Mock
    private EnvironmentUtil environmentUtil;

    @InjectMocks
    @Spy
    private BackupCoordinator backupCoordinator;

    @Mock
    private BackupRequester backupRequester;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        ClusterEntity mockClusterEntity = new ClusterEntity();
        mockClusterEntity.setKeyspaces(BackupCoordinatorTest.KEYSPACE);
        when(clusterEntityRepository.findByClusterName(NODEAGENT1_CLUSTER_NAME)).thenReturn(mockClusterEntity);
        when(nodeEntityRepository.findByClusterName(NODEAGENT1_CLUSTER_NAME)).thenReturn(createTestNodeEntities());
    }

    @Test
    public void requestBackup_sendRequestToMultipleNodes_successss() throws Exception {
        ArrayList<String> empty = new ArrayList<>();
        Map<String, PingStatus> testNodeStatuses = new HashMap<>();
        testNodeStatuses.put(NodeEntityData.NODEAGENT1_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT2_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT3_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT4_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT5_IP, PingStatus.OK);
        doReturn(empty).when(pingService).findUnhealthyNodes(NODEAGENT1_CLUSTER_NAME);
        backupCoordinator.setPingService(pingService);
        String actualResponseString = backupCoordinator.requestBackup(BackupType.INCREMENTAL_BACKUP, NODEAGENT1_CLUSTER_NAME);
        String expectedResponseString = SUCCESS_STATUS;
        assertThat(actualResponseString, equalTo(expectedResponseString));
    }

    @Test
    public void requestBackup_sendRequestToMultipleNodesAndTwoOfThemIsDown_fail() throws Exception {
        List<String> downNodes = new ArrayList<>();
        downNodes.add(NodeEntityData.NODEAGENT2_IP);
        downNodes.add(NodeEntityData.NODEAGENT1_IP);
        Map<String, PingStatus> testNodeStatuses = new HashMap<>();
        testNodeStatuses.put(NodeEntityData.NODEAGENT1_IP, PingStatus.DOWN);
        testNodeStatuses.put(NodeEntityData.NODEAGENT2_IP, PingStatus.DOWN);
        testNodeStatuses.put(NodeEntityData.NODEAGENT3_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT4_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT5_IP, PingStatus.OK);
        doReturn(downNodes).when(pingService).findUnhealthyNodes(NODEAGENT1_CLUSTER_NAME);
        backupCoordinator.setPingService(pingService);
        String actualResponseString = backupCoordinator.requestBackup(BackupType.INCREMENTAL_BACKUP, NODEAGENT1_CLUSTER_NAME);
        String expectedResponseString = String.format(BackupCoordinatorTest.ERROR_TEMPLATE, String.join(", ", downNodes));
        assertThat(actualResponseString, equalTo(expectedResponseString));
    }

    @Test
    public void findUnhealthyNodes_pingMultipleNodes_allNodesAreUp() {
        Map<String, PingStatus> testNodeStatuses = new HashMap<>();
        testNodeStatuses.put(NodeEntityData.NODEAGENT1_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT2_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT3_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT4_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT5_IP, PingStatus.OK);
        List<String> actualDownNodeIPList = pingService.findUnhealthyNodes(NODEAGENT1_CLUSTER_NAME);
        assertThat(actualDownNodeIPList.size(), equalTo(0));
    }

    @Test
    public void findUnhealthyNodes_pingMultipleNodes_twoNodesAreDown() {
        Map<String, PingStatus> testNodeStatuses = new HashMap<>();
        testNodeStatuses.put(NodeEntityData.NODEAGENT1_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT2_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT3_IP, PingStatus.OK);
        testNodeStatuses.put(NodeEntityData.NODEAGENT4_IP, PingStatus.DOWN);
        testNodeStatuses.put(NodeEntityData.NODEAGENT5_IP, PingStatus.DOWN);
        PingService pingServiveSpy = new PingService(nodeEntityRepository, mock(PingRequester.class));
        PingService psSpy = spy(pingServiveSpy);
        doReturn(testNodeStatuses).when(psSpy).pingNodes(NODEAGENT1_CLUSTER_NAME);
        List<String> actualDownNodeIPList = psSpy.findUnhealthyNodes(NODEAGENT1_CLUSTER_NAME);
        List<String> expectedDownNodeIPList = new ArrayList<>();
        expectedDownNodeIPList.add(NodeEntityData.NODEAGENT4_IP);
        expectedDownNodeIPList.add(NodeEntityData.NODEAGENT5_IP);
        assertThat(actualDownNodeIPList, containsInAnyOrder(expectedDownNodeIPList.toArray()));
    }

    @Test
    public void scheduleBackup_checkIsActiveStatus_twoReuqestSent() {
        List<ScheduleEntity> testScheduleData = getTestScheduleData();
        when(scheduleEntityRepository.findAll()).thenReturn(testScheduleData);
        backupCoordinator.setCurrentTime("02:00");
        backupCoordinator.setDayOfMonth(1);
        doNothing().when(backupCoordinator).fillTodaysDateInfo();
        doReturn("").when(backupCoordinator).requestBackup(anyString(), anyString());
        backupCoordinator.scheduleBackup();
        Mockito.verify(backupCoordinator, times(2)).requestBackup(anyString(), anyString());
    }

    private List<ScheduleEntity> getTestScheduleData() {
        List<ScheduleEntity> testScheduleData = new ArrayList<>();
        testScheduleData.add(getScheduleEntityData(true));
        testScheduleData.add(getScheduleEntityData(true));
        testScheduleData.add(getScheduleEntityData(false));
        return testScheduleData;
    }

    private ScheduleEntity getScheduleEntityData(final Boolean isActive) {
        Set<Integer> backupDays = new HashSet<>();
        backupDays.add(1);
        backupDays.add(16);
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setBackupHour("02:00");
        scheduleEntity.setIsActive(isActive);
        scheduleEntity.setSnapshotDays(backupDays);
        scheduleEntity.setClusterName(CLUSTER_1);
        return scheduleEntity;
    }

    private List<NodeEntity> createTestNodeEntities() {
        NodeEntityTestDataCreator testDataCreator = new NodeEntityTestDataCreator();
        List<NodeEntity> testData = testDataCreator
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
                .setNodeEntityIpAddress(NODEAGENT1_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT2_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT2_NODE_NAME)
                .setNodeEntityIpAddress(NODEAGENT2_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT3_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT3_NODE_NAME)
                .setNodeEntityIpAddress(NODEAGENT3_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT4_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT4_NODE_NAME)
                .setNodeEntityIpAddress(NODEAGENT4_IP)
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