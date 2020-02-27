package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.entity.BackupType;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.event.CommandSuccessInAllNodesEvent;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.EnvironmentUtil;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.RandomStringUtil;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/*
    This service handles CommandSuccessInAllNodesEvent for backup and initiates copy.
    It inherits from ApplicationListener, so that when event CommandSuccessInAllNodesEvent
    is published, this class will catch it at call onApplicationEvent method to perform
    it's duty.
 */
@Service
public class CopyBackupsCoordinator implements ApplicationListener<CommandSuccessInAllNodesEvent> {
    private static final String COPY_INFO = "Sending copy command to agents for backupRelation ";
    private static final String NOT_ENOUGH_SPACE_TEMPLATE = "No space available at %s even if old backups were deleted.";
    private static final String UNEXPECTED_ERROR = "Something happened while sending copyBackups command to agents";
    private static final String NO_SNAPSHOT_DIRECTORY_FOR_INCREMENTAL = "There isn't any backup directories found in %s to put the incremental backup inside.";
    private static final String DELETING_INFO_TEMPLATE = "Deleting old backups %s to make room for new backup.";

    private static final Logger LOGGER = LoggerFactory.getLogger(CopyBackupsCoordinator.class);
    private static final String BACKUP_FOLDER_NAME_TEMPLATE = "backup%04d";

    private CommandResultRepository commandResultRepository;
    private ClusterBackupCounterRepository clusterBackupCounterRepository;
    private CopyBackupRequester copyBackupRequester;

    private NodeEntityRepository nodeEntityRepository;
    private ClusterEntityRepository clusterEntityRepository;

    @Value("${nodeagent.backup.cleanOldBackups}")
    private Boolean deleteOldBackups;

    /*
        This function is interested in successful backup operations and it is called automatically by
        ApplicationListener when CommandSuccessInAllNodesEvent is raised from another component of
        the application.
     */
    @Override
    public void onApplicationEvent(final CommandSuccessInAllNodesEvent commandSuccessInAllNodesEvent) {
        if (commandSuccessInAllNodesEvent.getCommandName().equals(CommandResultConstants.CommandNames.BACKUP)) {
            copyBackupFiles(commandSuccessInAllNodesEvent.getRelation());
        }
    }

    /*
        Checks whether it can perform the copy operation successfully, and if it can,
        it sends the copy request to all nodes.
     */
    public void copyBackupFiles(final String backupRelation) {
        try {
            getLogger().info(CopyBackupsCoordinator.COPY_INFO + backupRelation);
            List<CommandResult> commandResults = commandResultRepository.findByRelation(backupRelation);
            // assuming there is a single cluster for each relation
            String clusterName = commandResults.get(0).getClusterName();
            ClusterEntity clusterFromDB = clusterEntityRepository.findByClusterName(clusterName);
            long totalBackupSize = getTotalBackupSize(commandResults);
            long usableSpace = getUsableSpace(Paths.get(clusterFromDB.getBackupPath()));
            boolean haveEnoughSpaceForBackup = usableSpace > totalBackupSize;
            // Send the request to nodes for copy either if target copy location has enough space or
            // it can make space by deleting old backups.
            if (haveEnoughSpaceForBackup || canMakeEnoughSpaceForBackupByDeletingOldBackups(clusterName, totalBackupSize, clusterFromDB.getBackupPath())) {
                if (!haveEnoughSpaceForBackup) {
                    List<File> backupFilesToBeDeleted = findBackupFilesToBeDeleted(clusterName, totalBackupSize, clusterFromDB.getBackupPath());
                    deleteOldBackupsToMakeRoom(backupFilesToBeDeleted);
                }
                sendCopyRequestsToAllNodes(commandResults, clusterFromDB.getBackupPath());
            } else {
                getLogger().error(String.format(CopyBackupsCoordinator.NOT_ENOUGH_SPACE_TEMPLATE, clusterFromDB.getBackupPath()));
            }
        } catch (final Exception e) {
            getLogger().error(CopyBackupsCoordinator.UNEXPECTED_ERROR, e);
        }
    }

    protected void sendCopyRequestsToAllNodes(final List<CommandResult> backupCommandResults, final String backupPath) {
        String clusterName = backupCommandResults.get(0).getClusterName();
        String backupType = backupCommandResults.get(0).
                getMoreInfo().get(CommandResultConstants.MoreInfoNames.BACKUP_TYPE);
        String backupDirectoryName = determineBackupDirectoryName(clusterName, backupType, backupPath);
        String copyRelation = RandomStringUtil.generateAlphanumericString(CommandResultConstants.RELATION_LENGTH);

        for (CommandResult backupResult : backupCommandResults) {
            CopyArgs copyArgs = createCopyArgs(clusterName, backupDirectoryName, copyRelation, backupResult, backupPath);
            NodeEntity nodeEntity = nodeEntityRepository.findByClusterNameAndNodeName(clusterName,
                    backupResult.getNodeName());
            sendCopyRequestToNode(nodeEntity, copyArgs);
        }

    }

    private void sendCopyRequestToNode(final NodeEntity nodeEntity, final CopyArgs copyArgs) {
        CommandResult copyResult = null;
        try {
            copyResult = copyBackupRequester.requestCopyFromNode(nodeEntity, copyArgs);
        } finally {
            commandResultRepository.save(copyResult);
        }
    }


    public CopyArgs createCopyArgs(final String clusterName, final String backupFolderName, final String copyRelation, final CommandResult backupResult, final String backupPath) {
        Path sourcePath = Paths.get(backupResult.getMoreInfo().get(
                CommandResultConstants.MoreInfoNames.BACKUP_FILE_PATH));
        Path destinationPath = Paths.get(backupPath,
                clusterName,
                backupFolderName,
                sourcePath.getFileName().toString());
        return new CopyArgs(sourcePath.toString(), destinationPath.toString(), copyRelation, deleteOldBackups);
    }

    /*
        Finds the suitable directory to put the backups in.
     */

    protected String determineBackupDirectoryName(final String clusterName, final String backupType, final String backupPath) {
        // Backup is an incremental backup. It puts the backup inside the latest snapshot directory, so that
        // it will be easier to track, delete old backups, and restoreBatch them.
        if (backupType.equals(BackupType.INCREMENTAL_BACKUP)) {
            String previousBackupDirectoryName = getPreviousBackupDirectoryName(clusterName, backupPath);
            if (previousBackupDirectoryName == null) {
                String message = String.format(CopyBackupsCoordinator.NO_SNAPSHOT_DIRECTORY_FOR_INCREMENTAL, backupType);
                getLogger().error(message);
                throw new IllegalStateException(message);
            }
            return previousBackupDirectoryName;
        }

        // Backup is a snapshot, so it creates a new directory to put the snapshot
        ClusterBackupCounter backupCounter = clusterBackupCounterRepository.findByClusterName(clusterName);
        int nextBackupNumber = 1;
        if (backupCounter == null) {
            backupCounter = new ClusterBackupCounter(clusterName, nextBackupNumber);
            clusterBackupCounterRepository.save(backupCounter);
        } else {
            nextBackupNumber = backupCounter.getBackupCount() + 1;
            backupCounter.setBackupCount(nextBackupNumber);
            clusterBackupCounterRepository.save(backupCounter);
        }
        return String.format(CopyBackupsCoordinator.BACKUP_FOLDER_NAME_TEMPLATE, nextBackupNumber);
    }

    private String getPreviousBackupDirectoryName(final String clusterName, final String backupPath) {
        File[] oldBackupDirectories = getOldBackupDirectoriesOfClusterAsSorted(clusterName, backupPath);
        if (oldBackupDirectories.length == 0) {
            // there are not any older backups.
            return null;
        }
        // Return the latest snpshot directory
        return oldBackupDirectories[oldBackupDirectories.length - 1].getName();
    }

    protected long getTotalBackupSize(final List<CommandResult> commandResults) {
        return commandResults.stream().mapToLong(c -> Long.parseLong(c.getMoreInfo().get(
                CommandResultConstants.MoreInfoNames.BACKUP_SIZE))).sum();
    }

    /*
        This function looks at old backup files (not including the latest one) in order to make space for new backup.
        If by deleting old backups can it make enough space for new backup, it returns true. The reason to not look
        at the latest backup is that if something goes wrong with taking the new backup, the user will have no backup
        to restoreBatch his/her database.
     */
    protected boolean canMakeEnoughSpaceForBackupByDeletingOldBackups(final String clusterName, long totalBackupSize, final String backupPath) throws IOException {
        File[] oldBackupDirectories = getOldBackupDirectoriesOfClusterAsSorted(clusterName, backupPath);
        if (oldBackupDirectories == null || oldBackupDirectories.length == 0) {
            // no old backups available to delete. cannot make room for the new backup.
            return false;
        }
        // Iterate over old backup directories excluding the latest one
        for (int i = 0; i < oldBackupDirectories.length - 1; i++) {
            File oldBackup = oldBackupDirectories[i];
            long oldBackupSize = getBackupSize(Paths.get(oldBackup.getPath()));
            totalBackupSize -= oldBackupSize;
            if (totalBackupSize < 0) {
                break;
            }
        }
        // it is not possible to make room for totalBackupSize even if older backups were deleted.
        // so do not delete anything and return false
        return totalBackupSize < 0;
    }

    /*
        If it determines that it can make space by eleting old backups, it finds the the backups
        that it should delete in order to make enough space.
     */
    protected List<File> findBackupFilesToBeDeleted(final String clusterName, long totalBackupSize, final String backupPath) throws IOException {
        File[] oldBackupDirectories = getOldBackupDirectoriesOfClusterAsSorted(clusterName, backupPath);
        List<File> backupFilesToBeDeleted = new ArrayList<>();
        for (int i = 0; i < oldBackupDirectories.length - 1; i++) {
            File oldBackup = oldBackupDirectories[i];
            long oldBackupSize = getBackupSize(Paths.get(oldBackup.getPath()));
            totalBackupSize -= oldBackupSize;
            backupFilesToBeDeleted.add(oldBackup);
            if (totalBackupSize < 0) {
                break;
            }
        }
        return backupFilesToBeDeleted;
    }

    // this method returns File array of old backup directories sorted by directory name (ascending).
    private File[] getOldBackupDirectoriesOfClusterAsSorted(final String clusterName, final String backupPath) {
        Path clusterBackupPath = Paths.get(backupPath, clusterName);
        if (!clusterBackupPath.toFile().exists()) {
            return new File[0];
        }
        File[] oldBackupDirectories = clusterBackupPath.toFile().listFiles(File::isDirectory);
        // sort the oldBackupDirectories to start deleting from oldest to newest
        Arrays.sort(oldBackupDirectories, Comparator.comparing(File::getName));
        return oldBackupDirectories;
    }

    private void deleteOldBackupsToMakeRoom(final List<File> backupFilesToBeDeleted) throws IOException {

        String[] oldBackupNames = backupFilesToBeDeleted.stream().map(f -> f.getName()).toArray(String[]::new);
        getLogger().info(String.format(CopyBackupsCoordinator.DELETING_INFO_TEMPLATE,
                Arrays.toString(oldBackupNames)));
        for (File backup : backupFilesToBeDeleted) {
            FileUtils.delete(Paths.get(backup.getPath()));
        }
    }

    private long getBackupSize(final Path path) throws IOException {
        return FileUtils.size(path);
    }

    protected Logger getLogger() {
        return CopyBackupsCoordinator.LOGGER;
    }

    protected long getUsableSpace(Path p) {
        while (p != null && !Files.exists(p)) {
            p = p.getParent();
        }
        if (p == null) {
            return 0;
        }
        File f = new File(p.toString());
        return f.getUsableSpace();
    }

    protected void setDeleteOldBackups(final boolean deleteOldBackups){
        this.deleteOldBackups = deleteOldBackups;
    }

    @Autowired
    protected void setCommandResultRepository(final CommandResultRepository commandResultRepository) {
        this.commandResultRepository = commandResultRepository;
    }

    @Autowired
    protected void setEnvironmentUtil(final EnvironmentUtil environmentUtil) {
        EnvironmentUtil environmentUtil1 = environmentUtil;
    }

    @Autowired
    protected void setClusterBackupCounterRepository(final ClusterBackupCounterRepository clusterBackupCounterRepository) {
        this.clusterBackupCounterRepository = clusterBackupCounterRepository;
    }

    @Autowired
    protected void setNodeEntityRepository(final NodeEntityRepository nodeEntityRepository) {
        this.nodeEntityRepository = nodeEntityRepository;
    }

    @Autowired
    public void setCopyBackupRequester(final CopyBackupRequester copyBackupRequester) {
        this.copyBackupRequester = copyBackupRequester;
    }

    @Autowired
    public void setClusterEntityRepository(final ClusterEntityRepository clusterEntityRepository) {
        this.clusterEntityRepository = clusterEntityRepository;
    }
}
