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
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto.ClusterDTO;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.CLUSTER_ALREADY_EXISTS_ERROR;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.CLUSTER_DOES_NOT_EXISTS_ERROR;

public class ClusterServiceTest {
    private static final String CLUSTER_1 = "cluster1";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private ClusterEntityRepository mockedClusterRepository;
    @InjectMocks
    private ClusterService clusterService;
    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void registerCluster_clusterExists_returnErrorMessage() throws Exception {
        ClusterEntity testClusterEntity = new ClusterEntity();
        testClusterEntity.setClusterName(ClusterServiceTest.CLUSTER_1);
        when(mockedClusterRepository.findByClusterName("cluster1")).thenReturn(new ClusterEntity());
        clusterService.setClusterEntityRepository(mockedClusterRepository);


        String actualResponse = clusterService.registerCluster(testClusterEntity);
        String expectedResult = String.format(CLUSTER_ALREADY_EXISTS_ERROR, ClusterServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResult));
    }

    @Test
    public void getClusterInfo_clusterDoesNotExists_returnNull() throws Exception {
        when(mockedClusterRepository.findByClusterName(ClusterServiceTest.CLUSTER_1)).thenReturn(null);
        clusterService.setClusterEntityRepository(mockedClusterRepository);
        ClusterEntity actualEntity = clusterService.getClusterInfo(ClusterServiceTest.CLUSTER_1);
        assertThat(actualEntity, equalTo(null));
    }

    @Test
    public void updateClusterBackupPath_clusterDoesNotExists_returnErrorMessage() throws Exception {
        ClusterEntity testClusterEntity = new ClusterEntity();
        testClusterEntity.setClusterName(ClusterServiceTest.CLUSTER_1);
        when(mockedClusterRepository.findByClusterName(ClusterServiceTest.CLUSTER_1)).thenReturn(null);
        clusterService.setClusterEntityRepository(mockedClusterRepository);
        String actualResponse = clusterService.updateBackupPathOnDB(testClusterEntity);
        assertThat(actualResponse, equalTo(String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, ClusterServiceTest.CLUSTER_1)));
    }

    @Test
    public void deleteCluster_clusterDoesNotExists_returnErrorMessage() throws Exception {
        ClusterDTO testClusterDTO = new ClusterDTO();
        testClusterDTO.setClusterName(ClusterServiceTest.CLUSTER_1);
        when(mockedClusterRepository.findByClusterName(ClusterServiceTest.CLUSTER_1)).thenReturn(null);
        clusterService.setClusterEntityRepository(mockedClusterRepository);
        String actualResponse = clusterService.deleteCluster(ClusterServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, ClusterServiceTest.CLUSTER_1)));
    }


}