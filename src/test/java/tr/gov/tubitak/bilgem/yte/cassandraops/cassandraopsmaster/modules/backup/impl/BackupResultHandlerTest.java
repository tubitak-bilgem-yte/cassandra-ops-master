package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.CommandResultTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.commandresult.CommandResultHandler;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.impl.exception.IllegalCommandResultStatusException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.BackupSizeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.BackupSizeEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class BackupResultHandlerTest {
    private static final String CLUSTER_NAME = "cluster1";
    private static final String RELATION = "abc123";
    private static final String NODE_NAME = "node1";
    private static final String MESSAGE = "message";

    private CommandResult commandResult;

    @Mock
    private NodeEntityRepository nodeEntityRepository;
    @Mock
    private CommandResultRepository commandResultRepository;
    @Mock
    private BackupSizeEntityRepository backupSizeEntityRepository;
    @Spy
    private CommandResultHandler commandResultHandler;
    @Mock
    private Logger logger;
    @InjectMocks
    @Spy
    private BackupResultHandler backupResultHandler;
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(commandResultRepository.save(any(CommandResult.class))).thenReturn(new CommandResult());
        when(backupSizeEntityRepository.save(any(BackupSizeEntity.class))).thenReturn(null);

    }

    @Test
    public void handle_CommandStatusIsError_fail() {

        commandResult = new CommandResult();
        commandResult.setStatus(CommandResultConstants.CommandStatus.ERROR);
        commandResult.setClusterName(BackupResultHandlerTest.CLUSTER_NAME);
        commandResult.setRelation(BackupResultHandlerTest.RELATION);
        commandResult.setNodeName(BackupResultHandlerTest.NODE_NAME);
        commandResult.setMoreInfo(new HashMap<>());
        when(commandResultRepository.findByRelationAndClusterNameAndNodeName(anyString(), anyString(), anyString())).thenReturn(commandResult);
        doReturn(logger).when(backupResultHandler).getLogger();
        backupResultHandler.handle(commandResult);

        verify(logger).error(anyString());
    }


    @Test(expected = IllegalCommandResultStatusException.class)
    public void handle_CommandStatusIsInvalid_throwEx() {
        String invalidString = "invalid_string";
        commandResult = new CommandResult();
        commandResult.setStatus(invalidString);
        backupResultHandler.handle(commandResult);
    }

    @Test
    public void handle_handleSuccessAllNodesAreNotDone_success() {
        commandResult = new CommandResult();
        Map<String, String> moreInfo = new HashMap<>();
        moreInfo.put("uncompressedTotalBackupSize", "0");
        commandResult.setMoreInfo(moreInfo);
        commandResult.setStatus(CommandResultConstants.CommandStatus.SUCCESS);
        commandResult.setClusterName(BackupResultHandlerTest.CLUSTER_NAME);
        commandResult.setRelation(BackupResultHandlerTest.RELATION);
        commandResult.setNodeName(BackupResultHandlerTest.NODE_NAME);
        doReturn(false).when(backupResultHandler).areAllNodesSucceeded(anyString());
        when(commandResultRepository.findByRelationAndClusterNameAndNodeName(anyString(), anyString(), anyString())).thenReturn(commandResult);
        doReturn(logger).when(backupResultHandler).getLogger();
        backupResultHandler.handle(commandResult);

        verify(logger).info(anyString());
    }

    @Test
    public void handle_handleSuccessAllNodesAreDone_success() {
        commandResult = new CommandResult();
        commandResult.setClusterName(BackupResultHandlerTest.CLUSTER_NAME);
        commandResult.setRelation(BackupResultHandlerTest.RELATION);
        commandResult.setNodeName(BackupResultHandlerTest.NODE_NAME);
        Map<String, String> moreInfo = new HashMap<>();
        moreInfo.put("uncompressedTotalBackupSize", "0");
        commandResult.setMoreInfo(moreInfo);
        when(commandResultRepository.findByRelationAndClusterNameAndNodeName(anyString(), anyString(), anyString())).thenReturn(commandResult);
        doReturn(true).when(backupResultHandler).areAllNodesSucceeded(anyString());
        commandResult.setStatus(CommandResultConstants.CommandStatus.SUCCESS);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        backupResultHandler.setApplicationEventPublisher(publisher);
        backupResultHandler.handle(commandResult);
    }

    @Test
    public void areAllNodesSucceeded_commandResultListLenghtIsNotEqualToNodeEntityListSize_fail() {
        List<NodeEntity> testNodeEntityData = createTestNodeEntitys();
        List<CommandResult> testCommandResultData = createCommandResults();
        testCommandResultData.remove(testCommandResultData.size() - 1);
        when(nodeEntityRepository.findByClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)).thenReturn(testNodeEntityData);
        when(commandResultRepository.findByRelation(BackupResultHandlerTest.RELATION)).thenReturn(testCommandResultData);

        boolean actualResult = backupResultHandler.areAllNodesSucceeded(BackupResultHandlerTest.RELATION);
        assertThat(actualResult, equalTo(false));
    }

    @Test
    public void areAllNodesSucceeded_notAllCommandsAreSucceded_success() {
        List<NodeEntity> testNodeEntityData = createTestNodeEntitys();
        List<CommandResult> testCommandResultData = createCommandResults();
        Random random = new Random();
        int ranDomCommandResultSelectionIndex = random.nextInt(testCommandResultData.size());
        testCommandResultData.get(ranDomCommandResultSelectionIndex).setStatus(CommandResultConstants.CommandStatus.ERROR);
        when(nodeEntityRepository.findByClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)).thenReturn(testNodeEntityData);
        when(commandResultRepository.findByRelation(BackupResultHandlerTest.RELATION)).thenReturn(testCommandResultData);

        boolean actualResult = backupResultHandler.areAllNodesSucceeded(BackupResultHandlerTest.RELATION);
        assertThat(actualResult, equalTo(false));
    }

    @Test
    public void areAllNodesSucceeded_allNodeOpperationsAreComplete_success() {
        List<NodeEntity> testNodeEntityData = createTestNodeEntitys();
        List<CommandResult> testCommandResultData = createCommandResults();
        when(nodeEntityRepository.findByClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)).thenReturn(testNodeEntityData);
        when(commandResultRepository.findByRelation(BackupResultHandlerTest.RELATION)).thenReturn(testCommandResultData);
        boolean actualResult = backupResultHandler.areAllNodesSucceeded(BackupResultHandlerTest.RELATION);
        assertThat(actualResult, equalTo(true));
    }




    private List<NodeEntity> createTestNodeEntitys() {
        NodeEntityTestDataCreator testDataCreator = new NodeEntityTestDataCreator();
        List<NodeEntity> testData = testDataCreator
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
                .setNodeEntityIpAddress(NodeEntityData.NODEAGENT1_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT2_NODE_NAME)
                .setNodeEntityIpAddress(NodeEntityData.NODEAGENT2_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT3_NODE_NAME)
                .setNodeEntityIpAddress(NodeEntityData.NODEAGENT3_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT4_NODE_NAME)
                .setNodeEntityIpAddress(NodeEntityData.NODEAGENT4_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT5_NODE_NAME)
                .setNodeEntityIpAddress(NodeEntityData.NODEAGENT5_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .build();
        return testData;
    }

    private List<CommandResult> createCommandResults() {
        CommandResultTestDataCreator testDataCreator = new CommandResultTestDataCreator();
        List<CommandResult> testDataCommandResults = testDataCreator
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(BackupResultHandlerTest.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(BackupResultHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(BackupResultHandlerTest.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(BackupResultHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT2_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(BackupResultHandlerTest.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(BackupResultHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT3_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(BackupResultHandlerTest.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(BackupResultHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT4_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(BackupResultHandlerTest.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(BackupResultHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT5_NODE_NAME)
                .build();
        return testDataCommandResults;
    }

}
