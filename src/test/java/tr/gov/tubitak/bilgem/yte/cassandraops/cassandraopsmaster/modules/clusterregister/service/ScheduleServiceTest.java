package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntityRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;

public class ScheduleServiceTest {
    private static final String CLUSTER_1 = "cluster1";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private ScheduleEntityRepository scheduleRepository;
    @Mock
    private ClusterEntityRepository clusterRepository;
    @InjectMocks
    private ScheduleService scheduleService;
    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void disableSchedule_scheduleDoesNotExists_returnErrorMessage() {
        when(scheduleRepository.findByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(null);
        String actualResponse = scheduleService.disableSchedule(ScheduleServiceTest.CLUSTER_1);
        String expectedResponse = String.format(SCHEDULE_DOES_NOT_EXISTS_ERROR, ScheduleServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void enableSchedule_scheduleDoesNotExists_returnErrorMessage() {
        when(scheduleRepository.findByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(null);
        scheduleService.setScheduleEntityRepository(scheduleRepository);
        String actualResponse = scheduleService.enableSchedule(ScheduleServiceTest.CLUSTER_1);
        String expectedResponse = String.format(SCHEDULE_DOES_NOT_EXISTS_ERROR, ScheduleServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void deleteSchedule_clusterDoesNotExists_returnErrorMessage() {
        when(clusterRepository.existsByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(false);
        scheduleService.setClusterEntityRepository(clusterRepository);
        String actualResponse = scheduleService.deleteSchedule(ScheduleServiceTest.CLUSTER_1);
        String expectedResponse = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, ScheduleServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void deleteSchedule_scheduleDoesNotExists_returnErrorMessage() {
        when(clusterRepository.existsByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(true);
        when(scheduleRepository.findByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(null);
        scheduleService.setClusterEntityRepository(clusterRepository);
        scheduleService.setScheduleEntityRepository(scheduleRepository);
        String actualResponse = scheduleService.deleteSchedule(ScheduleServiceTest.CLUSTER_1);
        String expectedResponse = String.format(SCHEDULE_DOES_NOT_EXISTS_ERROR, ScheduleServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void addSchedule_clusterDoesNotExists_returnErrorMessage() {
        when(clusterRepository.existsByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(false);
        scheduleService.setClusterEntityRepository(clusterRepository);
        String actualResponse = scheduleService.addSchedule(ScheduleServiceTest.CLUSTER_1, null);
        String expectedResponse = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, ScheduleServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void addSchedule_scheduleAlreadyExists_returnErrorMessage() {
        when(clusterRepository.existsByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(true);
        when(scheduleRepository.findByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(new ScheduleEntity());
        scheduleService.setClusterEntityRepository(clusterRepository);
        scheduleService.setScheduleEntityRepository(scheduleRepository);

        String actualResponse = scheduleService.addSchedule(ScheduleServiceTest.CLUSTER_1, null);
        String expectedResponse = String.format(SCHEDULE_ALREADY_EXISTS_ERROR, ScheduleServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void updateSchedule_scheduleDoesNotExists_returnErrorMessage() {
        when(scheduleRepository.findByClusterName(ScheduleServiceTest.CLUSTER_1)).thenReturn(null);
        scheduleService.setScheduleEntityRepository(scheduleRepository);

        String actualResponse = scheduleService.updateSchedule(ScheduleServiceTest.CLUSTER_1, null);
        String expectedResponse = String.format(SCHEDULE_DOES_NOT_EXISTS_ERROR, ScheduleServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }



}