package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.event.CommandSuccessInAllNodesEvent;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto.RestoreDTO;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.exceptions.NoOtherBackupsFoundException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service.RestoreService;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore.BatchRestoreResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestoreCoordinator implements ApplicationListener<CommandSuccessInAllNodesEvent> {


    private static final String RESTORE_SUCCES = "All restoreBatch operations have been complete successfully!";
    private RestoreService restoreService;
    private RestoreEntityRepository restoreEntityRepository;
    private Logger logger = LoggerFactory.getLogger(RestoreCoordinator.class);

    @Override
    public void onApplicationEvent(final CommandSuccessInAllNodesEvent commandSuccessInAllNodesEvent) {
        if (commandSuccessInAllNodesEvent.getCommandName().equals(CommandResultConstants.CommandNames.RESTORE)) {
            getLogger().info(commandSuccessInAllNodesEvent.getBatchRestoreResponse().getCommandResult().getMessage());

            BatchRestoreResponse batchRestoreResponse = commandSuccessInAllNodesEvent.getBatchRestoreResponse();
            RestoreEntity restoreEntity = restoreEntityRepository.findByParentRelation(batchRestoreResponse.getParentRelation());
            RestoreDTO restoreDTO = generateRestoreDTO(restoreEntity);
            restoreDTO.setClusterName(batchRestoreResponse.getCommandResult().getClusterName());
            restoreDTO.setNodeName(batchRestoreResponse.getCommandResult().getNodeName());
            try {
                String currentUniqueName = getCurrentUniqueName(restoreEntity.getUniqueBackupNames(), batchRestoreResponse.getCurrentUniqueBackupName());
                restoreService.restoreBatch(restoreDTO, currentUniqueName);
            } catch (final NoOtherBackupsFoundException e) {
                getLogger().info(RestoreCoordinator.RESTORE_SUCCES);
            }
        }
    }

    protected RestoreDTO generateRestoreDTO(final RestoreEntity restoreEntity) {
        RestoreDTO restoreDTO = new RestoreDTO();
        restoreDTO.setParentRelation(restoreEntity.getParentRelation());
        restoreDTO.setRestoreKeyspaces(new ArrayList<>(restoreEntity.getRestoreKeyspaces()));
        restoreDTO.setAllBackupFolders(new ArrayList<>(restoreEntity.getBackupPaths()));
        return restoreDTO;
    }


    protected String getCurrentUniqueName(final List<String> allUniqueNames, final String previousUniqueName) throws NoOtherBackupsFoundException {
        int previousUniqueNameIndex = allUniqueNames.indexOf(previousUniqueName);
        int uniqueNamesLimit = allUniqueNames.size() - 1;

        if (previousUniqueNameIndex > -1 && previousUniqueNameIndex != uniqueNamesLimit) {
            int currentUniqueNameIndex = previousUniqueNameIndex + 1;
            return allUniqueNames.get(currentUniqueNameIndex);
        } else {
            throw new NoOtherBackupsFoundException("No other backups found.");
        }


    }


    @Autowired
    protected void setRestoreEntityRepository(final RestoreEntityRepository restoreEntityRepository) {
        this.restoreEntityRepository = restoreEntityRepository;
    }

    @Autowired
    public void setRestoreService(final RestoreService restoreService) {
        this.restoreService = restoreService;
    }

    protected Logger getLogger() {
        return logger;
    }
}
