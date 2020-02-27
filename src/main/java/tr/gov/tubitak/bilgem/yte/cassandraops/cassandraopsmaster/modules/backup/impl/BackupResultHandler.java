package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.commandresult.CommandResultHandler;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.event.CommandSuccessInAllNodesEvent;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.BackupSizeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.BackupSizeEntityRepository;


/*
    When all of the nodes return with success messages for backup, it publishes an event for all listeners to perform their actions.
 */
@Service
public class BackupResultHandler extends CommandResultHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(BackupResultHandler.class);

    private ApplicationEventPublisher applicationEventPublisher;
    private BackupSizeEntityRepository backupSizeEntityRepository;

    @Override
    protected void handleSuccess(final CommandResult commandResult) {
        saveToDb(commandResult);
        getLogger().info(String.format(CommandResultHandler.NODE_SUCCESS_TEMPLATE,
                commandResult.getCommandName(),
                commandResult.getRelation(),
                commandResult.getClusterName(),
                commandResult.getNodeName()));
        long uncompressedTotalBackupSize = 0;
        BackupSizeEntity backupSizeEntity = backupSizeEntityRepository.findByRelationName(commandResult.getRelation());
        if (backupSizeEntity == null) {
            backupSizeEntity = new BackupSizeEntity(uncompressedTotalBackupSize, commandResult.getRelation());
            backupSizeEntityRepository.save(backupSizeEntity);
        } else {
            backupSizeEntity.setTotalSize(backupSizeEntity.getTotalSize() + uncompressedTotalBackupSize);
            backupSizeEntityRepository.save(backupSizeEntity);

        }
        // If all of the nodes return with a success response, call performAllNodesSuccessOperations
        if (areAllNodesSucceeded(commandResult.getRelation())) {
            performAllNodesSuccessOperations(commandResult);
        }
    }

    @Override
    protected void performAllNodesSuccessOperations(final CommandResult commandResult) {
        super.performAllNodesSuccessOperations(commandResult);
        // when all backups are successfull, publish an event so that backup files can be copied.
        // This is supposed to be async.
        applicationEventPublisher.publishEvent(
                new CommandSuccessInAllNodesEvent(this, commandResult.getRelation(), getCommandName()));
    }

    @Autowired
    public void setBackupSizeEntityRepository(final BackupSizeEntityRepository backupSizeEntityRepository) {
        this.backupSizeEntityRepository = backupSizeEntityRepository;
    }

    @Autowired
    protected void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public String getCommandName() {
        return CommandResultConstants.CommandNames.BACKUP;
    }

    @Override
    protected Logger getLogger() {
        return BackupResultHandler.LOGGER;
    }
}
