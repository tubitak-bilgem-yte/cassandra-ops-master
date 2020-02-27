package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntityRepository;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterTestData.CLUSTER_1;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterTestData.OPSMASTER_BACKUP_PATH;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ScheduleServiceIT {

    @Autowired
    private ScheduleEntityRepository scheduleEntityRepository;
    @Autowired
    private ClusterEntityRepository clusterEntityRepository;

    private ScheduleService scheduleService;
    private ClusterEntity defaultClusterEntity;
    private ScheduleEntity defaultScheduleEntity;

    @Before
    public void init() {
        initializeScheduleInfo();
        createClusterRegisterInfo();
    }

    @Test
    public void disableSchedule_setIsActiveToFalse_returnSuccessMessage() {
        clusterEntityRepository.save(defaultClusterEntity);

        String actualResponse = scheduleService.disableSchedule(CLUSTER_1);
        String expectedResponse = String.format(SCHEDULE_DISABLE_SUCCESS_MESSAGE, CLUSTER_1);
        ScheduleEntity scheduleFromDB = scheduleEntityRepository.findByClusterName(CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));
        assertThat(scheduleFromDB.getIsActive(), equalTo(false));
    }

    @Test
    public void enableSchedule_setIsActiveToTrue_returnSuccessMessage() {
        clusterEntityRepository.save(defaultClusterEntity);

        String actualResponse = scheduleService.enableSchedule(CLUSTER_1);
        String expectedResponse = String.format(SCHEDULE_ENABLE_SUCCESS_MESSAGE, CLUSTER_1);
        ScheduleEntity scheduleFromDB = scheduleEntityRepository.findByClusterName(CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));
        assertThat(scheduleFromDB.getIsActive(), equalTo(true));
    }

    @Test
    public void addSchedule_addScheduleToDB_returnSuccessMessage() {
        defaultClusterEntity.setSchedule(null);
        clusterEntityRepository.save(defaultClusterEntity);

        String actualResponse = scheduleService.addSchedule(CLUSTER_1, defaultScheduleEntity);
        String expectedResponse = String.format(SCHEDULE_ADD_SUCCESS_MESSAGE, CLUSTER_1);
        ScheduleEntity scheduleFromDB = scheduleEntityRepository.findByClusterName(CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));
        assertThat(scheduleFromDB, equalTo(defaultScheduleEntity));
    }

    @Test
    public void deleteSchedule_deleteGivenSchedule_returnSuccessMessage() {
        clusterEntityRepository.save(defaultClusterEntity);

        String actualResponse = scheduleService.deleteSchedule(CLUSTER_1);
        String expectedResponse = String.format(SCHEDULE_DELETE_SUCCESS_MESSAGE, CLUSTER_1);
        ScheduleEntity scheduleFromDB = scheduleEntityRepository.findByClusterName(CLUSTER_1);
        ClusterEntity clusterFromDB = clusterEntityRepository.findByClusterName(CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));
        assertThat(scheduleFromDB, equalTo(null));
        assertThat(clusterFromDB.getSchedule(), equalTo(null));
    }

    @Test
    public void updateSchedule_updateAllFields_returnSuccessMessage() {
        clusterEntityRepository.save(defaultClusterEntity);

        ScheduleEntity updatedScheduleEntity = new ScheduleEntity();
        updatedScheduleEntity.setIsActive(false);
        updatedScheduleEntity.setBackupHour("03:00");
        Set<Integer> updatedSnapshotBackupDays = new HashSet<>();
        updatedSnapshotBackupDays.add(2);
        updatedSnapshotBackupDays.add(17);
        updatedSnapshotBackupDays.add(22);
        updatedScheduleEntity.setSnapshotDays(updatedSnapshotBackupDays);
        updatedScheduleEntity.setClusterName(CLUSTER_1);

        String actualResponse = scheduleService.updateSchedule(CLUSTER_1, updatedScheduleEntity);
        String expectedResponse = String.format(SCHEDULE_UPDATE_SUCCESS_MESSAGE, CLUSTER_1);
        ScheduleEntity scheduleFromDB = scheduleEntityRepository.findByClusterName(CLUSTER_1);

        assertThat(actualResponse, equalTo(expectedResponse));
        assertThat(scheduleFromDB, equalTo(updatedScheduleEntity));
    }

    private void createClusterRegisterInfo() {
        defaultScheduleEntity = new ScheduleEntity();
        defaultScheduleEntity.setBackupHour("02:00");
        defaultScheduleEntity.setIsActive(true);
        defaultScheduleEntity.setClusterName(CLUSTER_1);
        Set<Integer> testSnapshotDays = new HashSet<>();
        testSnapshotDays.add(1);
        testSnapshotDays.add(16);
        defaultScheduleEntity.setSnapshotDays(testSnapshotDays);

        defaultClusterEntity = new ClusterEntity();
        defaultClusterEntity.setClusterName(CLUSTER_1);
        defaultClusterEntity.setBackupPath(OPSMASTER_BACKUP_PATH);
        defaultClusterEntity.setSchedule(defaultScheduleEntity);

    }

    private void initializeScheduleInfo() {
        scheduleService = new ScheduleService();
        scheduleService.setClusterEntityRepository(clusterEntityRepository);
        scheduleService.setScheduleEntityRepository(scheduleEntityRepository);
    }
}
