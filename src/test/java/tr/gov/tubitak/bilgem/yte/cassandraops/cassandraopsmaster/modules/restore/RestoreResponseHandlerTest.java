package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.CommandResultTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore.BatchRestoreResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestoreResponseHandlerTest {
    private static final String CLUSTER_NAME = "cluster1";
    private static final String COMMAND_RESULT_RELATION = "abc123";
    private static final String PARENT_RELATION = "abc123";
    private static final String NODE_NAME = "node1";
    private static final String MESSAGE = "message";
    @Mock
    private CommandResult commandResult;
    @Mock
    private CommandResultRepository commandResultRepository;
    @Mock
    private RestoreEntityRepository restoreEntityRepository;
    @InjectMocks
    @Spy
    private RestoreResponseHandler restoreResponseHandler;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(commandResultRepository.save(any(CommandResult.class))).thenReturn(new CommandResult());
    }


    @Test
    public void handle_CommandStatusIsError_fail() {
        commandResult = new CommandResult();
        commandResult.setStatus(CommandResultConstants.CommandStatus.ERROR);
        commandResult.setClusterName(RestoreResponseHandlerTest.CLUSTER_NAME);
        commandResult.setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION);
        commandResult.setNodeName(RestoreResponseHandlerTest.NODE_NAME);
        commandResult.setMessage(RestoreResponseHandlerTest.MESSAGE);
        commandResult.setMoreInfo(new HashMap<>());
        BatchRestoreResponse batchRestoreResponse = new BatchRestoreResponse();
        batchRestoreResponse.setCommandResult(commandResult);
        restoreResponseHandler.handle(batchRestoreResponse);
    }


    @Test
    public void handle_handleSuccess_allNodesAreDone_success() {
        commandResult = new CommandResult();
        commandResult.setStatus(CommandResultConstants.CommandStatus.SUCCESS);
        commandResult.setClusterName(RestoreResponseHandlerTest.CLUSTER_NAME);
        commandResult.setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION);
        commandResult.setNodeName(RestoreResponseHandlerTest.NODE_NAME);
        commandResult.setMoreInfo(new HashMap<>());
        doReturn(true).when(restoreResponseHandler).areAllNodesSucceeded(anyString(),anyString());
        BatchRestoreResponse batchRestoreResponse = new BatchRestoreResponse();
        batchRestoreResponse.setCommandResult(commandResult);
        batchRestoreResponse.setParentRelation(RestoreResponseHandlerTest.PARENT_RELATION);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        restoreResponseHandler.setApplicationEventPublisher(publisher);
        restoreResponseHandler.handle(batchRestoreResponse);
    }


    @Test
    public void handle_handleSuccess_allNodesAreNotDone_success() {
        commandResult = new CommandResult();
        commandResult.setStatus(CommandResultConstants.CommandStatus.SUCCESS);
        commandResult.setClusterName(RestoreResponseHandlerTest.CLUSTER_NAME);
        commandResult.setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION);
        commandResult.setNodeName(RestoreResponseHandlerTest.NODE_NAME);
        commandResult.setMoreInfo(new HashMap<>());
        doReturn(false).when(restoreResponseHandler).areAllNodesSucceeded(anyString(),anyString());
        BatchRestoreResponse batchRestoreResponse = new BatchRestoreResponse();
        batchRestoreResponse.setCommandResult(commandResult);
        batchRestoreResponse.setParentRelation(RestoreResponseHandlerTest.PARENT_RELATION);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        restoreResponseHandler.setApplicationEventPublisher(publisher);
        restoreResponseHandler.handle(batchRestoreResponse);
    }


    @Test
    public void areAllNodesSucceeded_commandResultListLenghtIsNotEqualToRestoreNodeCount_fail() {
        List<CommandResult> testCommandResultData = createCommandResults();
        RestoreEntity restoreEntity = new RestoreEntity();
        restoreEntity.setRestoreNodeCount((short) (testCommandResultData.size() - 1));
        when(commandResultRepository.findByRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)).thenReturn(testCommandResultData);
        when(restoreEntityRepository.findByParentRelation(RestoreResponseHandlerTest.PARENT_RELATION)).thenReturn(restoreEntity);
        boolean actualResult = restoreResponseHandler.areAllNodesSucceeded(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION, RestoreResponseHandlerTest.PARENT_RELATION);
        assertThat(actualResult, equalTo(false));
    }

    @Test
    public void areAllNodesSucceeded_notAllCommandsAreSucceded_fail() {
        List<CommandResult> testCommandResultData = createCommandResults();
        RestoreEntity restoreEntity = new RestoreEntity();
        restoreEntity.setRestoreNodeCount((short) (testCommandResultData.size()));
        Random random = new Random();
        int ranDomCommandResultSelectionIndex = random.nextInt(testCommandResultData.size());
        testCommandResultData.get(ranDomCommandResultSelectionIndex).setStatus(CommandResultConstants.CommandStatus.ERROR);
        when(restoreEntityRepository.findByParentRelation(RestoreResponseHandlerTest.PARENT_RELATION)).thenReturn(restoreEntity);
        when(commandResultRepository.findByRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)).thenReturn(testCommandResultData);
        boolean actualResult = restoreResponseHandler.areAllNodesSucceeded(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION, RestoreResponseHandlerTest.PARENT_RELATION);
        assertThat(actualResult, equalTo(false));
    }

    @Test
    public void areAllNodesSucceeded_allNodeOperationsAreComplete_success() {
        List<CommandResult> testCommandResultData = createCommandResults();
        RestoreEntity restoreEntity = new RestoreEntity();
        restoreEntity.setRestoreNodeCount((short) (testCommandResultData.size()));
        when(restoreEntityRepository.findByParentRelation(RestoreResponseHandlerTest.PARENT_RELATION)).thenReturn(restoreEntity);
        when(commandResultRepository.findByRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)).thenReturn(testCommandResultData);
        boolean actualResult = restoreResponseHandler.areAllNodesSucceeded(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION, RestoreResponseHandlerTest.PARENT_RELATION);
        assertThat(actualResult, equalTo(true));
    }


    private List<CommandResult> createCommandResults() {
        CommandResultTestDataCreator testDataCreator = new CommandResultTestDataCreator();
        List<CommandResult> testDataCommandResults = testDataCreator
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(RestoreResponseHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(RestoreResponseHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT2_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(RestoreResponseHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT3_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(RestoreResponseHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT4_NODE_NAME)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(RestoreResponseHandlerTest.COMMAND_RESULT_RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(RestoreResponseHandlerTest.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT5_NODE_NAME)
                .build();
        return testDataCommandResults;
    }

}
