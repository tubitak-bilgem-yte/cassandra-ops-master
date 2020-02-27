package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.context.annotation.PropertySource;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.CommandResultTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.entity.BackupType;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api.CopyBackupRequester;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api.entity.CopyArgs;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.persistence.ClusterBackupCounter;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.persistence.ClusterBackupCounterRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
public class CopyBackupsCoordinatorIT {
    private static final String CLUSTER = "cluster1";
    private static final String RELATION = "abc123";
    private static final String MESSAGE = "message";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private CommandResultRepository commandResultRepository;
    @Mock
    private ClusterBackupCounterRepository clusterBackupCounterRepository;
    @Mock
    private CopyBackupRequester copyBackupRequester;
    @Mock
    private NodeEntityRepository nodeEntityRepository;
    @Mock
    private ClusterEntityRepository clusterEntityRepository;
    @InjectMocks
    @Spy
    private CopyBackupsCoordinator copyBackupsCoordinator = new CopyBackupsCoordinator();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        ClusterEntityRepository clusterEntityRepository = mock(ClusterEntityRepository.class);
        ClusterEntity mockClusterEntityForBackup = new ClusterEntity();
        mockClusterEntityForBackup.setBackupPath(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        when(clusterEntityRepository.findByClusterName(anyString())).thenReturn(mockClusterEntityForBackup);
        copyBackupsCoordinator.setClusterEntityRepository(clusterEntityRepository);
    }

    @After
    public void after() throws IOException {
        Path path = Paths.get(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.ROOT_FOLDER);
        if (!path.toFile().exists()) {
            return;
        }
        FileUtils.delete(path);
    }

    @Test
    public void canMakeEnoughSpaceForBackupByDeletingOldBackups_noOldBackupDirectoryFound_returnsFalse() throws Exception {
        long backupSize = 5000L;

        boolean actualResult = copyBackupsCoordinator.canMakeEnoughSpaceForBackupByDeletingOldBackups(CopyBackupsCoordinatorIT.CLUSTER, backupSize, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        assertThat(actualResult, equalTo(false));
    }

    @Test
    public void canMakeEnoughSpaceForBackupByDeletingOldBackups_backupDirectoryIsEmpty_returnsFalse() throws Exception {
        long backupSize = 5000L;

        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        mainDirectory.mkdirs();
        boolean actualResult = copyBackupsCoordinator.canMakeEnoughSpaceForBackupByDeletingOldBackups(CopyBackupsCoordinatorIT.CLUSTER, backupSize, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        assertThat(actualResult, equalTo(false));
    }


    @Test
    public void canMakeEnoughSpaceForBackupByDeletingOldBackups_deletingOldBackupsIsNotEnough_returnsFalse() throws Exception {
        long backupSize = 100000L;

        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER);
        mainDirectory.mkdirs();
        createBackupFilesWithSizes();
        boolean actualResult = copyBackupsCoordinator.canMakeEnoughSpaceForBackupByDeletingOldBackups(CopyBackupsCoordinatorIT.CLUSTER, backupSize, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        assertThat(actualResult, equalTo(false));
    }

    @Test
    public void canMakeEnoughSpaceForBackupByDeletingOldBackups_deletingOldBackupsIsEnough_returnsTrue() throws Exception {
        long backupSize = 1000L;

        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER);
        mainDirectory.mkdirs();
        createBackupFilesWithSizes();
        boolean actualResult = copyBackupsCoordinator.canMakeEnoughSpaceForBackupByDeletingOldBackups(CopyBackupsCoordinatorIT.CLUSTER, backupSize, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        assertThat(actualResult, equalTo(true));
    }

    @Test
    public void findBackupFilesToBeDeleted_checkToBeDeletedFilesAreCorrect_success() throws Exception {
        String existingFileName_1 = "backups001";
        String existingFileName_2 = "backups002";
        long backupSize = 9000L;

        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER);
        mainDirectory.mkdirs();
        createBackupFilesWithSizes();

        List<String> expectedDeletionFiles = new ArrayList<>();
        expectedDeletionFiles.add(existingFileName_1);
        expectedDeletionFiles.add(existingFileName_2);
        List<File> actualFileList = copyBackupsCoordinator.findBackupFilesToBeDeleted(CopyBackupsCoordinatorIT.CLUSTER, backupSize, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        List<String> actualFileNameList = actualFileList.stream()
                .map(File::getName)
                .collect(Collectors.toList());
        assertThat(actualFileNameList, equalTo(expectedDeletionFiles));
    }

    @Test
    public void copyBackupFiles_checkOldBackupsAreDeletedCorrectly_success() throws IOException {
        long totalBackupSize = 9000L;
        long usableSpace = 1000L;
        String expectedRemainingFileName_1 = "backups003";
        String expectedRemainingFileName_2 = "backups004";
        createBackupFilesWithSizes();
        doReturn(totalBackupSize).when(copyBackupsCoordinator).getTotalBackupSize(anyList());
        doReturn(usableSpace).when(copyBackupsCoordinator).getUsableSpace(any());
        CommandResultRepository commandResultRepository = mock(CommandResultRepository.class);
        copyBackupsCoordinator.setCommandResultRepository(commandResultRepository);
        List<CommandResult> testDataCreator = new CommandResultTestDataCreator().addCommandResult()
                .setClusterName(CopyBackupsCoordinatorIT.CLUSTER)
                .build();
        when(commandResultRepository.findByRelation(anyString())).thenReturn(testDataCreator);
        copyBackupsCoordinator.copyBackupFiles(CopyBackupsCoordinatorIT.RELATION);

        File[] clusterFile = new File(CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER).listFiles();
        List<String> actualFileNameList = Arrays.stream(clusterFile).map(File::getName).collect(Collectors.toList());
        List<String> expectedFileNameList = new ArrayList<>();
        expectedFileNameList.add(expectedRemainingFileName_1);
        expectedFileNameList.add(expectedRemainingFileName_2);
        assertThat(actualFileNameList, equalTo(expectedFileNameList));
    }

    @Test(expected = IllegalStateException.class)
    public void determineBackupDirectoryName_IncrementalBackupNoBackupDirectory_throwEx() {
        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER);
        mainDirectory.mkdirs();
        copyBackupsCoordinator.determineBackupDirectoryName(CopyBackupsCoordinatorIT.CLUSTER, BackupType.INCREMENTAL_BACKUP, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
    }

    @Test
    public void determineBackupDirectoryName_IncrementalBackup_success() throws IOException {
        String backupDirectoryName = "backups004";

        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER);
        mainDirectory.mkdirs();
        createBackupFilesWithSizes();
        String actualLatestBackupDirectoryName = copyBackupsCoordinator.determineBackupDirectoryName(CopyBackupsCoordinatorIT.CLUSTER, BackupType.INCREMENTAL_BACKUP, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        assertThat(actualLatestBackupDirectoryName, equalTo(backupDirectoryName));
    }

    @Test
    public void determineBackupDirectoryName_FirstSnapshotCreation_success() throws IOException {
        int initialClusterBackupCounter = 1;
        String backupDirectoryName = "backup0001";

        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER);
        mainDirectory.mkdirs();
        ClusterBackupCounterRepository repository = mock(ClusterBackupCounterRepository.class);
        when(repository.findByClusterName(CopyBackupsCoordinatorIT.CLUSTER)).thenReturn(null);
        when(repository.save(any(ClusterBackupCounter.class))).thenReturn(new ClusterBackupCounter(CopyBackupsCoordinatorIT.CLUSTER, initialClusterBackupCounter));
        copyBackupsCoordinator.setClusterBackupCounterRepository(repository);
        String actualLatestBackupDirectoryName = copyBackupsCoordinator.determineBackupDirectoryName(CopyBackupsCoordinatorIT.CLUSTER, BackupType.SNAPSHOT, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        assertThat(actualLatestBackupDirectoryName, equalTo(backupDirectoryName));
    }

    @Test
    public void determineBackupDirectoryName_HavePreviousBackupFolders_success() throws IOException {
        int initialClusterBackupCounter = 3;
        int afterTestClusterBackupCounter = 4;
        String backupDirectoryName = "backup0004";


        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER);
        mainDirectory.mkdirs();
        ClusterBackupCounterRepository repository = mock(ClusterBackupCounterRepository.class);
        when(repository.findByClusterName(CopyBackupsCoordinatorIT.CLUSTER)).thenReturn(new ClusterBackupCounter(CopyBackupsCoordinatorIT.CLUSTER, initialClusterBackupCounter));
        when(repository.save(any(ClusterBackupCounter.class))).thenReturn(new ClusterBackupCounter(CopyBackupsCoordinatorIT.CLUSTER, afterTestClusterBackupCounter));
        copyBackupsCoordinator.setClusterBackupCounterRepository(repository);
        String actualLatestBackupDirectoryName = copyBackupsCoordinator.determineBackupDirectoryName(CopyBackupsCoordinatorIT.CLUSTER, BackupType.SNAPSHOT, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.CLUSTER_DIRECTORY);
        assertThat(actualLatestBackupDirectoryName, equalTo(backupDirectoryName));
    }

    @Test
    public void sendCopyRequestsToAllNodes_SendRequestToMultipleNodes_success() throws IOException {
        when(nodeEntityRepository.findByClusterNameAndNodeName(anyString(), anyString())).thenReturn(new NodeEntity());
        when(copyBackupRequester.requestCopyFromNode(any(NodeEntity.class), any(CopyArgs.class))).thenReturn(new CommandResult());
        when(commandResultRepository.save(any(CommandResult.class))).thenReturn(new CommandResult());

        List<CommandResult> testDataCommandResults = createCommandResults();
        createBackupFilesWithSizes();
        copyBackupsCoordinator.setDeleteOldBackups(true);
        copyBackupsCoordinator.sendCopyRequestsToAllNodes(testDataCommandResults, CopyBackupsTestData.CLUSTER_DIRECTORY);
        verify(nodeEntityRepository, times(5)).findByClusterNameAndNodeName(anyString(), anyString());
        verify(copyBackupRequester, times(5)).requestCopyFromNode(any(NodeEntity.class), any(CopyArgs.class));
        verify(commandResultRepository, times(5)).save(any(CommandResult.class));


    }

    private void createBackupFilesWithSizes() throws IOException {
        int smallFileSize = 1000;
        int mediumFileSize = 2000;
        int largeFileSize = 3000;

        File mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP1_FOLDER);
        mainDirectory.mkdirs();
        createRandomFileWithSize(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP1_FILE1, mediumFileSize);
        createRandomFileWithSize(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP1_FILE2, largeFileSize);

        mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP2_FOLDER);
        mainDirectory.mkdirs();
        createRandomFileWithSize(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP2_FILE1, mediumFileSize);
        createRandomFileWithSize(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP2_FILE2, largeFileSize);

        mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP3_FOLDER);
        mainDirectory.mkdirs();
        createRandomFileWithSize(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP3_FILE1, largeFileSize);

        mainDirectory = new File(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP4_FOLDER);
        mainDirectory.mkdirs();
        createRandomFileWithSize(tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP4_FILE1, smallFileSize);

    }

    private void createRandomFileWithSize(final String path, final long size) throws IOException {
        RandomAccessFile randomBackupFileWithSize;
        randomBackupFileWithSize = new RandomAccessFile(path, "rw");
        randomBackupFileWithSize.setLength(size);
        randomBackupFileWithSize.close();
    }

    private List<CommandResult> createCommandResults() {
        CommandResultTestDataCreator testDataCreator = new CommandResultTestDataCreator();
        List<CommandResult> testDataCommandResults = testDataCreator
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(CopyBackupsCoordinatorIT.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(CopyBackupsCoordinatorIT.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_TYPE, BackupType.INCREMENTAL_BACKUP)
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_FILE_PATH, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP4_FOLDER)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(CopyBackupsCoordinatorIT.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(CopyBackupsCoordinatorIT.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT2_NODE_NAME)
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_FILE_PATH, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP4_FOLDER)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(CopyBackupsCoordinatorIT.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(CopyBackupsCoordinatorIT.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT3_NODE_NAME)
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_FILE_PATH, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP4_FOLDER)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(CopyBackupsCoordinatorIT.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(CopyBackupsCoordinatorIT.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT4_NODE_NAME)
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_FILE_PATH, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP4_FOLDER)
                .addCommandResult()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setCommandName(CommandResultConstants.CommandNames.BACKUP)
                .setRelation(CopyBackupsCoordinatorIT.RELATION)
                .setStatus(CommandResultConstants.CommandStatus.SUCCESS)
                .setMessage(CopyBackupsCoordinatorIT.MESSAGE)
                .setNodeName(NodeEntityData.NODEAGENT5_NODE_NAME)
                .addMoreInfo(CommandResultConstants.MoreInfoNames.BACKUP_FILE_PATH, tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl.CopyBackupsTestData.BACKUP4_FOLDER)
                .build();
        return testDataCommandResults;
    }
    
}
