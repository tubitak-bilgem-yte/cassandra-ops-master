package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.event.CommandSuccessInAllNodesEvent;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto.RestoreDTO;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.exceptions.NoOtherBackupsFoundException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service.RestoreService;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore.BatchRestoreResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestoreCoordinatorTest {

    private static String RESTORE_ENTITY_PARENT_RELATION = "relation1";
    private static String COMMENT_RESULT_MESSAGE = "message";
    @Mock
    RestoreService restoreService;
    @Mock
    private RestoreEntityRepository restoreEntityRepository;
    @Mock
    private CommandSuccessInAllNodesEvent commandSuccessInAllNodesEvent;
    @InjectMocks
    @Spy
    private RestoreCoordinator restoreCoordinator;
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void onApplicationEvent_commandNameDoesNotEqualtoRestore_fail() throws NoOtherBackupsFoundException {
        RestoreResponseHandler restoreResponseHandler = new RestoreResponseHandler();
        BatchRestoreResponse batchRestoreResponse = new BatchRestoreResponse();
        String commandName = "notrestore";
        commandSuccessInAllNodesEvent = new CommandSuccessInAllNodesEvent(restoreResponseHandler, batchRestoreResponse, commandName);
        restoreCoordinator.onApplicationEvent(commandSuccessInAllNodesEvent);
    }

    @Test
    public void onApplicationEvent_commandNameEqualtoRestore_success() throws NoOtherBackupsFoundException {
        String commandName = "restoreBatch";
        RestoreEntity restoreEntityTEST = generateRestoreEntity();
        when(restoreEntityRepository.findByParentRelation(any())).thenReturn(restoreEntityTEST);
        commandSuccessInAllNodesEvent = generateCommandSuccessInAllNodesEvent(commandName, restoreEntityTEST.getUniqueBackupNames().get(0));
        restoreCoordinator.onApplicationEvent(commandSuccessInAllNodesEvent);
    }


    @Test
    public void onApplicationEvent_NoOtherBackupsFound_success() throws NoOtherBackupsFoundException {
        String commandName = "restoreBatch";
        RestoreEntity restoreEntityTEST = generateRestoreEntity();
        when(restoreEntityRepository.findByParentRelation(any())).thenReturn(restoreEntityTEST);
        List<String> uniqueNames = restoreEntityTEST.getUniqueBackupNames();
        commandSuccessInAllNodesEvent = generateCommandSuccessInAllNodesEvent(commandName, uniqueNames.get(uniqueNames.size() - 1));
        restoreCoordinator.onApplicationEvent(commandSuccessInAllNodesEvent);
    }

    @Test
    public void getCurrentUniqueName_AfterFirstBatchCase_success() throws NoOtherBackupsFoundException {
        List<String> allUniqueNames = generateStringList();
        String previousUniqueName = allUniqueNames.get(0);
        String actualCurrentUniqueName = restoreCoordinator.getCurrentUniqueName(allUniqueNames, previousUniqueName);
        assertThat(actualCurrentUniqueName, equalTo(allUniqueNames.get(1)));
    }

    @Test(expected = NoOtherBackupsFoundException.class)
    public void getCurrentUniqueName_AllBackupFilesWereRestored_success() throws NoOtherBackupsFoundException {
        List<String> allUniqueNames = generateStringList();
        String previousUniqueName = allUniqueNames.get(allUniqueNames.size() - 1);
        restoreCoordinator.getCurrentUniqueName(allUniqueNames, previousUniqueName);
    }

    @Test(expected = NoOtherBackupsFoundException.class)
    public void getCurrentUniqueName_TheLastBackupNameWasNotFoundInTheAllBackupList_fail() throws NoOtherBackupsFoundException {
        List<String> allUniqueNames = generateStringList();
        String previousUniqueName = "notUniqueName";
        restoreCoordinator.getCurrentUniqueName(allUniqueNames, previousUniqueName);
    }

    @Test
    public void generateRestorDTO_generateRestoreDTOfromRestoreEntity_success() {
        RestoreEntity restoreEntity = generateRestoreEntity();
        restoreCoordinator.generateRestoreDTO(restoreEntity);
    }

    private CommandSuccessInAllNodesEvent generateCommandSuccessInAllNodesEvent(final String commandName, final String currentUniqueName) {
        CommandResult commandResult = new CommandResult();
        commandResult.setMessage(RestoreCoordinatorTest.COMMENT_RESULT_MESSAGE);

        BatchRestoreResponse batchRestoreResponse = new BatchRestoreResponse();
        batchRestoreResponse.setCommandResult(commandResult);
        batchRestoreResponse.setParentRelation(RestoreCoordinatorTest.RESTORE_ENTITY_PARENT_RELATION);
        batchRestoreResponse.setCurrentUniqueBackupName(currentUniqueName);
        RestoreResponseHandler restoreResponseHandler = new RestoreResponseHandler();
        CommandSuccessInAllNodesEvent commandSuccessInAllNodesEvent = new CommandSuccessInAllNodesEvent(restoreResponseHandler,
                batchRestoreResponse, commandName);
        return commandSuccessInAllNodesEvent;

    }

    private RestoreEntity generateRestoreEntity() {
        RestoreEntity restoreEntity = new RestoreEntity();
        restoreEntity.setParentRelation(RestoreCoordinatorTest.RESTORE_ENTITY_PARENT_RELATION);
        restoreEntity.setBackupPaths(new HashSet<>(generateStringList()));
        restoreEntity.setRestoreKeyspaces(new HashSet<>(generateStringList()));
        restoreEntity.setUniqueBackupNames(generateStringList());
        return restoreEntity;
    }

    private List<String> generateStringList() {

        return new ArrayList<String>() {{
            add("string1");
            add("string2");
            add("string3");
            add("string4");
            add("string5");
        }};
    }


}
