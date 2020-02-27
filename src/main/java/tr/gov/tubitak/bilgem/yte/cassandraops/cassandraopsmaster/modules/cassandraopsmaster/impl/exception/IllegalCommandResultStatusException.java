package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.impl.exception;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;

public class IllegalCommandResultStatusException extends IllegalStateException {
    private static final String COMMAND_NAME_ILLEGAL = "commandResult status (%s) is illegal. Command name: %s. Relation: %s. Node: %s.%s";

    public IllegalCommandResultStatusException(final CommandResult commandResult) {
        super(String.format(IllegalCommandResultStatusException.COMMAND_NAME_ILLEGAL,
                commandResult.getStatus(),
                commandResult.getCommandName(),
                commandResult.getRelation(),
                commandResult.getClusterName(),
                commandResult.getNodeName()));
    }
}
