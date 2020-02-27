package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto.NodeDTO;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;

public class NodeServiceTest {
    private static final String CONSTRAINT_CHECK_ALL_FIELDS_NULL_ERROR = "The following fields cannot be null: nodeName,tempBackupPath,ip,port";

    private static final String TEST_BACKUP_PATH = "/NFS/backups";
    private static final String TEST_IP = "178.0.0.1";
    private static final int TEST_PORT = 28050;
    private static final String CLUSTER_1 = "cluster1";
    private static final String NODE_1 = "node1";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private ClusterEntityRepository clusterRepository;
    @Mock
    private NodeEntityRepository nodeRepository;
    @Mock
    private ModelMapper modelMapper;
    
    @InjectMocks
    private NodeService nodeService;
    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void addNode_clusterDoesNotExists_returnErrorMessage() throws Exception {
        when(clusterRepository.existsByClusterName(NodeServiceTest.CLUSTER_1)).thenReturn(false);
        nodeService.setClusterEntityRepository(clusterRepository);
        String actualResponse = nodeService.addNode(NodeServiceTest.CLUSTER_1, new NodeEntity());
        String expectedResponse = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, NodeServiceTest.CLUSTER_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void addNode_nodeAlreadyExists_returnErrorMessage() throws Exception {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setNodeName(NodeServiceTest.NODE_1);
        when(clusterRepository.existsByClusterName(NodeServiceTest.CLUSTER_1)).thenReturn(true);
        when(nodeRepository.findByClusterNameAndNodeName(NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1)).thenReturn(new NodeEntity());
        nodeService.setClusterEntityRepository(clusterRepository);
        nodeService.setNodeEntityRepository(nodeRepository);
        String actualResponse = nodeService.addNode(NodeServiceTest.CLUSTER_1, nodeEntity);
        String expectedResponse = String.format(NODE_ALREADY_EXISTS_ERROR, NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void addNode_nodeDoesNotSatisfyNotNullConstraints_returnErrorMessage() throws Exception {
        NodeEntity nodeEntity = new NodeEntity();
        when(clusterRepository.existsByClusterName(NodeServiceTest.CLUSTER_1)).thenReturn(true);
        when(nodeRepository.findByClusterNameAndNodeName(NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1)).thenReturn(null);
        nodeService.setClusterEntityRepository(clusterRepository);
        nodeService.setNodeEntityRepository(nodeRepository);
        String actualResponse = nodeService.addNode(NodeServiceTest.CLUSTER_1, nodeEntity);
        assertThat(actualResponse, equalTo(NodeServiceTest.CONSTRAINT_CHECK_ALL_FIELDS_NULL_ERROR));
    }

    @Test
    public void addNode_nodeAddedSuccessfully_returnSuccessMessage() throws Exception {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setNodeName(NodeServiceTest.NODE_1);
        nodeEntity.setTempBackupPath(NodeServiceTest.TEST_BACKUP_PATH);
        nodeEntity.setIp(NodeServiceTest.TEST_IP);
        nodeEntity.setPort(NodeServiceTest.TEST_PORT);

        when(clusterRepository.existsByClusterName(NodeServiceTest.CLUSTER_1)).thenReturn(true);
        when(nodeRepository.findByClusterNameAndNodeName(NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1)).thenReturn(null);
        when(modelMapper.map(any(NodeDTO.class), (Class<NodeEntity>) any(NodeEntity.class.getClass()))).thenReturn(new NodeEntity());
        nodeService.setClusterEntityRepository(clusterRepository);
        nodeService.setNodeEntityRepository(nodeRepository);
        String actualResponse = nodeService.addNode(NodeServiceTest.CLUSTER_1, nodeEntity);
        assertThat(actualResponse, equalTo(String.format(NODE_ADD_SUCCESS_MESSAGE, NodeServiceTest.NODE_1, NodeServiceTest.CLUSTER_1)));
    }

    @Test
    public void deleteNode_nodeDoesNotExists_returnErrorMessage() throws Exception {
        when(nodeRepository.findByClusterNameAndNodeName(NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1)).thenReturn(null);
        nodeService.setNodeEntityRepository(nodeRepository);
        String actualResponse = nodeService.deleteNode(NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1);
        String expectedResponse = String.format(NODE_DOES_NOT_EXISTS, NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }

    @Test
    public void updateNode_nodeDoesNotExists_returnErrorMessage() throws Exception {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setNodeName(NodeServiceTest.NODE_1);
        when(nodeRepository.findByClusterNameAndNodeName(NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1)).thenReturn(null);
        nodeService.setNodeEntityRepository(nodeRepository);
        String actualResponse = nodeService.updateNode(NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1, nodeEntity);
        String expectedResponse = String.format(NODE_DOES_NOT_EXISTS, NodeServiceTest.CLUSTER_1, NodeServiceTest.NODE_1);
        assertThat(actualResponse, equalTo(expectedResponse));
    }



}