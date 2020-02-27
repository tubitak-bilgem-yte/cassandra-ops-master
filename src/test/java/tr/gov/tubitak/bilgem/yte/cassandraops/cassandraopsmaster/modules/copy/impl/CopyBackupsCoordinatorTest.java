package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.CommandResultTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class CopyBackupsCoordinatorTest {

    private CommandResultTestDataCreator testDataCreator;
    private List<CommandResult> commandResultList;

    @Spy
    private CopyBackupsCoordinator copyBackupsCoordinator;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void getTotalBackupSize_testWithSixNodes_success() throws Exception {
        final String testDataBackupSize = "1000";
        long expectedBackupSize = 6000L;

        testDataCreator = new CommandResultTestDataCreator();
        commandResultList = testDataCreator
                .addCommandResult()
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_SIZE, testDataBackupSize)
                .addCommandResult()
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_SIZE, testDataBackupSize)
                .addCommandResult()
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_SIZE, testDataBackupSize)
                .addCommandResult()
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_SIZE, testDataBackupSize)
                .addCommandResult()
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_SIZE, testDataBackupSize)
                .addCommandResult()
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_SIZE, testDataBackupSize)
                .build();

        assertThat(copyBackupsCoordinator.getTotalBackupSize(commandResultList), equalTo(expectedBackupSize));

    }

}