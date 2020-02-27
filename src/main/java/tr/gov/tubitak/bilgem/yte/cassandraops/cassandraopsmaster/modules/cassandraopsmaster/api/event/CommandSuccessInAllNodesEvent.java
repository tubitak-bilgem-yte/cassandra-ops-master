package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.RestoreResponseHandler;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore.BatchRestoreResponse;

// This event can be used for notifying interested components
// when an operation (e.g., backup) is successful in all nodes.
@Getter
public class CommandSuccessInAllNodesEvent extends ApplicationEvent {
    private String relation;
    private String commandName;
    private CommandResult commandResult;
    private BatchRestoreResponse batchRestoreResponse;

    public CommandSuccessInAllNodesEvent(final Object source, final String relation, final String commandName) {
        super(source);
        this.relation = relation;
        this.commandName = commandName;
    }

    public CommandSuccessInAllNodesEvent(final RestoreResponseHandler source, final BatchRestoreResponse batchRestoreResponse, final String commandName) {
        super(source);
        this.batchRestoreResponse = batchRestoreResponse;
        this.commandName = commandName;
    }


}
