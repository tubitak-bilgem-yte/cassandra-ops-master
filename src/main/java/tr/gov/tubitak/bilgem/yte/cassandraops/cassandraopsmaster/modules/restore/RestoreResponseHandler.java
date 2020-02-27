package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.event.CommandSuccessInAllNodesEvent;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore.BatchRestoreResponse;

import java.util.List;

@Component
public class RestoreResponseHandler {
    protected static final String NODE_SUCCESS_TEMPLATE = "Command (%s, relation: %s) completed succesfully at node %s.%s";
    protected static final String ALL_NODE_SUCCESS_TEMPLATE = "%s command of relation (%s) completed successfully on all nodes";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreResponseHandler.class);
    private static final String NODE_ERROR_TEMPLATE = "Command %s failed at node %s with failure message: %s";
    protected CommandResultRepository commandResultRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private RestoreEntityRepository restoreEntityRepository;

    public String getCommandName() {
        return CommandResultConstants.CommandNames.RESTORE;
    }

    public void handle(final BatchRestoreResponse batchRestoreResponse) {
        if (batchRestoreResponse.getCommandResult().getStatus().equals(CommandResultConstants.CommandStatus.SUCCESS)) {
            getLogger().info(batchRestoreResponse.getCommandResult().getMessage());
            handleSuccess(batchRestoreResponse);
        } else {
            getLogger().error(batchRestoreResponse.getCommandResult().getMessage());
        }
    }

    private void handleSuccess(final BatchRestoreResponse batchRestoreResponse) {
        saveToDb(batchRestoreResponse.getCommandResult());
        getLogger().info(String.format(RestoreResponseHandler.NODE_SUCCESS_TEMPLATE,
                batchRestoreResponse.getCommandResult().getCommandName(),
                batchRestoreResponse.getCommandResult().getRelation(),
                batchRestoreResponse.getCommandResult().getClusterName(),
                batchRestoreResponse.getCommandResult().getNodeName()));
        // If all of the nodes return with a success response, call performAllNodesSuccessOperations
        if (areAllNodesSucceeded(batchRestoreResponse.getCommandResult().getRelation(), batchRestoreResponse.getParentRelation())) {
            performAllNodesSuccessOperations(batchRestoreResponse);
        }
    }

    protected boolean areAllNodesSucceeded(final String commandResultRelation, final String parentRelation) {
        List<CommandResult> results = commandResultRepository.findByRelation(commandResultRelation);
        RestoreEntity restoreEntity = restoreEntityRepository.findByParentRelation(parentRelation);

        // if total agent count and total result count do not match than all nodes are not succeeded yet
        if (restoreEntity.getRestoreNodeCount() != results.size()) {
            return false;
        }

        for (CommandResult commandResult : results) {
            if (!commandResult.getStatus().equals(CommandResultConstants.CommandStatus.SUCCESS)) {
                return false;
            }
        }
        return true;
    }

    private void saveToDb(final CommandResult commandResult) {
        commandResultRepository.save(commandResult);
    }

    private void performAllNodesSuccessOperations(final BatchRestoreResponse batchRestoreResponse) {

        getLogger().info(
                String.format(RestoreResponseHandler.ALL_NODE_SUCCESS_TEMPLATE,
                        getCommandName(), batchRestoreResponse.getCommandResult().getRelation()));

        applicationEventPublisher.publishEvent(
                new CommandSuccessInAllNodesEvent(this, batchRestoreResponse, batchRestoreResponse.getCommandResult().getCommandName()));
    }


    protected Logger getLogger() {
        return RestoreResponseHandler.LOGGER;
    }

    @Autowired
    protected void setCommandResultRepository(final CommandResultRepository commandResultRepository) {
        this.commandResultRepository = commandResultRepository;
    }

    @Autowired
    protected void setRestoreEntityRepository(final RestoreEntityRepository restoreEntityRepository) {
        this.restoreEntityRepository = restoreEntityRepository;
    }

    @Autowired
    protected void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


}
