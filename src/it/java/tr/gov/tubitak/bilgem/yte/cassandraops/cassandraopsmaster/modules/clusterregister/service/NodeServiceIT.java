package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.CassandraOpsMasterApplication;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData.*;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterTestData.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CassandraOpsMasterApplication.class)
@TestPropertySource(locations = "classpath:integrationtest-application.properties")
public class NodeServiceIT {

    @Autowired
    private ClusterEntityRepository clusterEntityRepository;
    @Autowired
    private NodeEntityRepository nodeEntityRepository;
    @Autowired
    private NodeService nodeRegisterInfoService;


    @After
    public void cleanUp() {
        clusterEntityRepository.deleteAll();
    }

    @Test
    public void addNode_addNodeToGivenCluster_returnSuccessMessage() {
        createClusterRegisterInfo();

        NodeEntity newNodeEntity = new NodeEntity();
        newNodeEntity.setIp(NODEAGENT6_IP);
        newNodeEntity.setPort(NodeEntityData.NODEAGENT_PORT);
        newNodeEntity.setNodeName(NODEAGENT6_NODE_NAME);
        newNodeEntity.setTempBackupPath(TEST_TEMP_BACKUP_PATH);
        newNodeEntity.setClusterName(CLUSTER_1);

        String actualResponseMessage = nodeRegisterInfoService.addNode(CLUSTER_1, newNodeEntity);
        String expectedResponseMessage = String.format(NODE_ADD_SUCCESS_MESSAGE, NODEAGENT6_NODE_NAME, CLUSTER_1);
        assertThat(actualResponseMessage, equalTo(expectedResponseMessage));

        NodeEntity nodeFromDB = nodeEntityRepository.findByClusterNameAndNodeName(CLUSTER_1, NODEAGENT6_NODE_NAME);

        Assert.assertEquals(newNodeEntity, nodeFromDB);

    }

    @Test
    public void addNode_updateNodeInfo_returnSuccessMessage() {
        createClusterRegisterInfo();

        NodeEntity newNodeEntity = new NodeEntity();
        newNodeEntity.setIp(NODEAGENT6_IP);
        newNodeEntity.setPort(NodeEntityData.NODEAGENT_UPDATED_PORT);
        newNodeEntity.setNodeName(NODEAGENT6_NODE_NAME);
        newNodeEntity.setTempBackupPath(UPDATED_BACKUP_PATH);
        newNodeEntity.setClusterName(CLUSTER_1);

        String actualResponseMessage = nodeRegisterInfoService.updateNode(CLUSTER_1, NODEAGENT5_NODE_NAME, newNodeEntity);
        String expectedResponseMessage = String.format(NODE_UPDATE_SUCCESS_MESSAGE, NODEAGENT5_NODE_NAME, CLUSTER_1);
        assertThat(actualResponseMessage, equalTo(expectedResponseMessage));

        NodeEntity nodeFromDB = nodeEntityRepository.findByClusterNameAndNodeName(CLUSTER_1, NodeEntityData.NODEAGENT6_NODE_NAME);

        Assert.assertEquals(newNodeEntity, nodeFromDB);

    }

    @Test
    public void deleteNode_deleteAGivenNode_returnSuccessMessage() {
        createClusterRegisterInfo();

        String actualResponseMessage = nodeRegisterInfoService.deleteNode(CLUSTER_1, NODEAGENT5_NODE_NAME);
        String expectedResponseMessage = String.format(DELETE_NODE_SUCCESS_MESSAGE, NODEAGENT5_NODE_NAME, CLUSTER_1);
        assertThat(actualResponseMessage, equalTo(expectedResponseMessage));

        List<NodeEntity> nodesFromDB = nodeEntityRepository.findByClusterName(CLUSTER_1);
        List<NodeEntity> testNodeEntityData = createDefaultNodeData();
        testNodeEntityData.remove(4);

        Assert.assertEquals(testNodeEntityData, nodesFromDB);
    }

    private void createClusterRegisterInfo() {
        ClusterEntity clusterEntity = new ClusterEntity();
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setBackupHour("02:00");
        scheduleEntity.setIsActive(true);
        Set<Integer> testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(1);
        testSnapshotDays.add(16);
        scheduleEntity.setSnapshotDays(testSnapshotDays);
        clusterEntity.setSchedule(scheduleEntity);
        clusterEntity.setNodes(createDefaultNodeData());
        clusterEntity.setClusterName(CLUSTER_1);
        clusterEntity.setBackupPath(OPSMASTER_BACKUP_PATH);
        clusterEntityRepository.save(clusterEntity);
    }

}