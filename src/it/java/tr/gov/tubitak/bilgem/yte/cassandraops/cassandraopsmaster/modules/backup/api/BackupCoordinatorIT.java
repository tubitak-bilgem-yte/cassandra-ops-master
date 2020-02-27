package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.entity.BackupType;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntityRepository;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterTestData.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
public class BackupCoordinatorIT {

    @Autowired
    private ScheduleEntityRepository scheduleEntityRepository;
    @Autowired
    private ClusterEntityRepository clusterEntityRepository;
    @InjectMocks
    @Spy
    private BackupCoordinator backupCoordinator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        backupCoordinator.setScheduleEntityRepository(scheduleEntityRepository);

    }

    @Test
    public void scheduleBackup_twoClustersMatchesTheHour_returnSuccessMessage() throws Exception {

        Set<Integer> testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(2);
        testSnapshotDays.add(15);
        createTestClusterEntity(CLUSTER_1, testSnapshotDays, "02:00");

        testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(2);
        testSnapshotDays.add(15);
        createTestClusterEntity(CLUSTER_2, testSnapshotDays, "02:00");

        testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(2);
        testSnapshotDays.add(16);
        createTestClusterEntity(CLUSTER_3, testSnapshotDays, "03:00");

        backupCoordinator.setCurrentTime("02:00");
        backupCoordinator.setDayOfMonth(15);
        doNothing().when(backupCoordinator).fillTodaysDateInfo();
        doReturn("").when(backupCoordinator).requestBackup(anyString(), anyString());
        backupCoordinator.scheduleBackup();
        Mockito.verify(backupCoordinator, Mockito.times(2)).requestBackup(anyString(), anyString());
    }

    @Test
    public void scheduleBackup_threeClusterMatchesHourTwoIsSnapshotOneIsIncremental_returnSuccessMessage() throws Exception {
        init();

        Set<Integer> testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(2);
        testSnapshotDays.add(15);
        createTestClusterEntity(CLUSTER_1, testSnapshotDays, "02:00");

        testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(2);
        testSnapshotDays.add(15);
        createTestClusterEntity(CLUSTER_2, testSnapshotDays, "02:00");

        testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(2);
        testSnapshotDays.add(16);
        createTestClusterEntity(CLUSTER_3, testSnapshotDays, "02:00");

        backupCoordinator.setCurrentTime("02:00");
        backupCoordinator.setDayOfMonth(15);
        doNothing().when(backupCoordinator).fillTodaysDateInfo();
        doReturn("").when(backupCoordinator).requestBackup(anyString(), anyString());
        backupCoordinator.scheduleBackup();
        Mockito.verify(backupCoordinator, Mockito.times(3)).requestBackup(anyString(), anyString());
        Mockito.verify(backupCoordinator, Mockito.times(2)).requestBackup(Mockito.eq(BackupType.SNAPSHOT), anyString());
        Mockito.verify(backupCoordinator, Mockito.times(1)).requestBackup(Mockito.eq(BackupType.INCREMENTAL_BACKUP), anyString());


    }




    private void createTestClusterEntity(final String clusterName, final Set<Integer> snapshotDays, final String backupTime) {
        ClusterEntity clusterEntity = new ClusterEntity();

        clusterEntity.setNodes(null);
        clusterEntity.setClusterName(clusterName);
        clusterEntity.setBackupPath(OPSMASTER_BACKUP_PATH);

        ScheduleEntity scheduleEntity = createTestScheduleEntity(clusterName, snapshotDays, backupTime);
        clusterEntity.setSchedule(scheduleEntity);
        clusterEntityRepository.save(clusterEntity);

    }

    private ScheduleEntity createTestScheduleEntity(final String clusterName, final Set<Integer> snapshotDays, final String backupTime) {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setBackupHour(backupTime);
        scheduleEntity.setIsActive(true);
        scheduleEntity.setSnapshotDays(snapshotDays);
        scheduleEntity.setClusterName(clusterName);

        return scheduleEntity;
    }


}

