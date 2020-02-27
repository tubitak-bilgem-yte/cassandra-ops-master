package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.commandresult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.impl.exception.IllegalCommandResultStatusException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;

import java.util.List;

public abstract class CommandResultHandler {
    protected static final String NODE_SUCCESS_TEMPLATE = "Command (%s, relation: %s) completed succesfully at node %s.%s";
    protected static final String ALL_NODE_SUCCESS_TEMPLATE = "%s command of relation (%s) completed successfully on all nodes";
    private static final String NODE_ERROR_TEMPLATE = "Command %s failed at node %s with failure message: %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandResultHandler.class);

    protected CommandResultRepository commandResultRepository;
    private NodeEntityRepository nodeEntityRepository;

    public void handle(final CommandResult commandResult) {
        if (commandResult.getStatus().equals(CommandResultConstants.CommandStatus.SUCCESS)) {
            handleSuccess(commandResult);
        } else if (commandResult.getStatus().equals(CommandResultConstants.CommandStatus.ERROR)) {
            handleError(commandResult);
        } else {
            throw new IllegalCommandResultStatusException(commandResult);
        }
    }

    public abstract String getCommandName();

    protected void handleSuccess(final CommandResult commandResult) {
        saveToDb(commandResult);
        getLogger().info(String.format(CommandResultHandler.NODE_SUCCESS_TEMPLATE,
                commandResult.getCommandName(),
                commandResult.getRelation(),
                commandResult.getClusterName(),
                commandResult.getNodeName()));
        // If all of the nodes return with a success response, call performAllNodesSuccessOperations
        if (areAllNodesSucceeded(commandResult.getRelation())) {
            performAllNodesSuccessOperations(commandResult);
        }
    }

    /*
        This is a base method for inherited classes to override. The operation that is done depends on
        what type of operation is performed.
    */
    protected void performAllNodesSuccessOperations(final CommandResult commandResult) {
        getLogger().info(
                String.format(CommandResultHandler.ALL_NODE_SUCCESS_TEMPLATE,
                        getCommandName(), commandResult.getRelation()));
    }

    private void handleError(final CommandResult commandResult) {
        saveToDb(commandResult);
        getLogger().error(String.format(CommandResultHandler.NODE_ERROR_TEMPLATE,
                commandResult.getCommandName(),
                commandResult.getNodeName(),
                commandResult.getMessage()));
    }

    public boolean areAllNodesSucceeded(final String relation) {
        List<CommandResult> results = commandResultRepository.findByRelation(relation);
        // find agents of cluster with relation to command
        List<NodeEntity> agents = nodeEntityRepository.findByClusterName(results.get(0).getClusterName());
        // if total agent count and total result count do not match than all nodes are not succeeded yet
        if (agents.size() != results.size()) {
            return false;
        }

        for (CommandResult commandResult : results) {
            if (!commandResult.getStatus().equals(CommandResultConstants.CommandStatus.SUCCESS)) {
                return false;
            }
        }
        return true;
    }

    // Update the status  of command result from RUNNING to SUCCESS
    protected void saveToDb(final CommandResult commandResult) {
        CommandResult commandResultDb = commandResultRepository.findByRelationAndClusterNameAndNodeName(
                commandResult.getRelation(),
                commandResult.getClusterName(),
                commandResult.getNodeName());
        commandResultDb.setStatus(commandResult.getStatus());
        commandResultDb.setMessage(commandResult.getMessage());
        commandResultDb.getMoreInfo().putAll(commandResult.getMoreInfo());
        commandResultRepository.save(commandResultDb);
    }

    protected Logger getLogger() {
        return CommandResultHandler.LOGGER;
    }


    @Autowired
    protected void setCommandResultRepository(final CommandResultRepository commandResultRepository) {
        this.commandResultRepository = commandResultRepository;
    }

    @Autowired
    protected void setNodeEntityRepository(final NodeEntityRepository nodeEntityRepository) {
        this.nodeEntityRepository = nodeEntityRepository;
    }
}
