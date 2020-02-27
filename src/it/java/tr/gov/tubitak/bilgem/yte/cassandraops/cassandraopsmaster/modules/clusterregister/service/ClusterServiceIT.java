package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterTestData.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ClusterServiceIT {

    @Autowired
    private ClusterEntityRepository clusterEntityRepository;
    @Autowired
    private ScheduleEntityRepository scheduleEntityRepository;
    @Autowired
    private NodeEntityRepository nodeEntityRepository;

    @Test
    public void registerCluster_registerClusterWithScheduleAndMultipleNodes_returnSuccessMessage() {
        ClusterService clusterService = createClusterService();
        ClusterEntity expectedDBObject = new ClusterEntity();
        expectedDBObject.setSchedule(createDefaultScheduleData());
        expectedDBObject.setNodes(createDefaultNodeData());
        expectedDBObject.setClusterName(CLUSTER_1);
        expectedDBObject.setBackupPath(OPSMASTER_BACKUP_PATH);

        String actualResponse = clusterService.registerCluster(expectedDBObject);
        String expectedResponse = String.format(CLUSTER_REGISTER_SUCCESS_MESSAGE, CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));

        ClusterEntity actualDBObject = clusterEntityRepository.findByClusterName(CLUSTER_1);
        // Since hamcrest can't do deep comparison for two objects, junit is used for this comparison
        Assert.assertEquals(expectedDBObject, actualDBObject);
    }

    @Test
    public void registerCluster_registerClusterWithoutScheduleAndMultipleNodes_returnSuccessMessage() {
        ClusterService clusterService = createClusterService();
        ClusterEntity expectedDBObject = new ClusterEntity();
        expectedDBObject.setSchedule(null);
        expectedDBObject.setNodes(createDefaultNodeData());
        expectedDBObject.setClusterName(CLUSTER_1);
        expectedDBObject.setBackupPath(OPSMASTER_BACKUP_PATH);

        String actualResponse = clusterService.registerCluster(expectedDBObject);
        String expectedResponse = String.format(CLUSTER_REGISTER_SUCCESS_MESSAGE, CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));

        ClusterEntity actualDBObject = clusterEntityRepository.findByClusterName(CLUSTER_1);

        Assert.assertEquals(expectedDBObject, actualDBObject);
    }

    @Test
    public void registerCluster_registerClusterWithScheduleAndWithoutNode_returnSuccessMessage() {
        ClusterService clusterService = createClusterService();
        ClusterEntity expectedDBObject = new ClusterEntity();
        expectedDBObject.setSchedule(createDefaultScheduleData());
        expectedDBObject.setClusterName(CLUSTER_1);
        expectedDBObject.setBackupPath(OPSMASTER_BACKUP_PATH);

        String actualResponse = clusterService.registerCluster(expectedDBObject);
        String expectedResponse = String.format(CLUSTER_REGISTER_SUCCESS_MESSAGE, CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));

        ClusterEntity actualDBObject = clusterEntityRepository.findByClusterName(CLUSTER_1);

        Assert.assertEquals(expectedDBObject, actualDBObject);
    }

    @Test
    public void registerCluster_registerClusterWithoutScheduleAndWithoutNode_returnSuccessMessage() {
        ClusterService clusterService = createClusterService();
        ClusterEntity expectedDBObject = new ClusterEntity();
        expectedDBObject.setClusterName(CLUSTER_1);
        expectedDBObject.setBackupPath(OPSMASTER_BACKUP_PATH);

        String actualResponse = clusterService.registerCluster(expectedDBObject);
        String expectedResponse = String.format(CLUSTER_REGISTER_SUCCESS_MESSAGE, CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));

        ClusterEntity actualDBObject = clusterEntityRepository.findByClusterName(CLUSTER_1);

        Assert.assertEquals(expectedDBObject, actualDBObject);
    }

    @Test
    public void getClusterInfo_getClusterWithScheduleAndWithMultipleNodes_returnSuccessMessage() throws Exception {
        ClusterService clusterService = createClusterService();
        ClusterEntity expectedEntity = createDefaultTestClusterData();
        clusterEntityRepository.save(expectedEntity);

        ClusterEntity actualClusterEntity = clusterService.getClusterInfo(CLUSTER_1);

        Assert.assertEquals(expectedEntity, actualClusterEntity);
    }

    @Test
    public void getClusterInfo_scheduleInfoIsNull_returnSuccessMessage() {
        ClusterService clusterService = createClusterService();
        ClusterEntity expectedEntity = createDefaultTestClusterData();
        expectedEntity.setSchedule(null);
        clusterEntityRepository.save(expectedEntity);

        ClusterEntity actualClusterEntity = clusterService.getClusterInfo(CLUSTER_1);

        Assert.assertEquals(expectedEntity, actualClusterEntity);
    }

    @Test
    public void getClusterInfo_nodeInfoIsNull_returnSuccessMessage() throws Exception {
        ClusterService clusterService = createClusterService();
        ClusterEntity expectedEntity = createDefaultTestClusterData();
        expectedEntity.setNodes(null);
        clusterEntityRepository.save(expectedEntity);

        ClusterEntity actualClusterEntity = clusterService.getClusterInfo(CLUSTER_1);

        Assert.assertEquals(expectedEntity, actualClusterEntity);
    }

    @Test
    public void updateClusterBackupPath_updateBackupPathWithNotNullValue_returnSuccessMessage() throws Exception {
        ClusterService clusterService = createClusterService();
        ClusterEntity clusterEntity = createDefaultTestClusterData();
        clusterEntityRepository.save(clusterEntity);

        clusterEntity.setBackupPath(UPDATED_BACKUP_PATH);

        String actualResponse = clusterService.updateBackupPathOnDB(clusterEntity);
        String expectedResponse = String.format(BACKUP_PATH_UPDATE_SUCCESS_MESSAGE, CLUSTER_1);
        ClusterEntity actualDBResult = clusterEntityRepository.findByClusterName(CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));
        Assert.assertEquals(clusterEntity, actualDBResult);
    }

    @Test
    public void deleteCluster_deleteClusterAndCheckScheduleAndNodesAreDeletedAsWell_returnSuccessMessage() throws Exception {
        ClusterService clusterService = createClusterService();

        ClusterEntity clusterEntity = createDefaultTestClusterData();
        clusterEntityRepository.save(clusterEntity);

        String actualResponse = clusterService.deleteCluster(CLUSTER_1);
        String expectedResponse = String.format(DELETE_CLUSTER_SUCCESS_MESSAGE, CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));

        ClusterEntity databaseClusterInfo = clusterEntityRepository.findByClusterName(CLUSTER_1);
        assertThat(databaseClusterInfo, equalTo(null));

        ScheduleEntity databaseScheduleInfo = scheduleEntityRepository.findByClusterName(CLUSTER_1);
        assertThat(databaseScheduleInfo, equalTo(null));

        List<NodeEntity> databaseNodeEntities = nodeEntityRepository.findByClusterName(CLUSTER_1);
        assertThat(databaseNodeEntities.size(), equalTo(0));

    }

    private ClusterService createClusterService() {
        ClusterService clusterService = new ClusterService();
        clusterService.setClusterEntityRepository(clusterEntityRepository);
        return clusterService;
    }

    private ClusterEntity createDefaultTestClusterData() {
        ClusterEntity clusterEntity = new ClusterEntity();
        clusterEntity.setSchedule(createDefaultScheduleData());
        clusterEntity.setNodes(createDefaultNodeData());
        clusterEntity.setClusterName(CLUSTER_1);
        clusterEntity.setBackupPath(OPSMASTER_BACKUP_PATH);

        return clusterEntity;
    }

    private ScheduleEntity createDefaultScheduleData() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setBackupHour("02:00");
        scheduleEntity.setIsActive(true);
        Set<Integer> testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(1);
        testSnapshotDays.add(16);
        scheduleEntity.setSnapshotDays(testSnapshotDays);
        scheduleEntity.setClusterName(CLUSTER_1);

        return scheduleEntity;
    }

}