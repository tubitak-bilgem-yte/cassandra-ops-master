package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandResultTestDataCreator {

    private List<CommandResult> commandResults;
    private CommandResult currentCommandResult;

    public CommandResultTestDataCreator() {
        commandResults = new ArrayList<>();
    }

    public CommandResultTestDataCreator addCommandResult() {
        currentCommandResult = new CommandResult();
        currentCommandResult.setMoreInfo(new HashMap<>());
        commandResults.add(currentCommandResult);
        return this;
    }

    public CommandResultTestDataCreator setCommandName(final String commandName) {
        currentCommandResult.setCommandName(commandName);
        return this;
    }

    public CommandResultTestDataCreator setRelation(final String relation) {
        currentCommandResult.setRelation(relation);
        return this;
    }

    public CommandResultTestDataCreator setStatus(final String status) {
        currentCommandResult.setStatus(status);
        return this;
    }

    public CommandResultTestDataCreator setMessage(final String message) {
        currentCommandResult.setMessage(message);
        return this;
    }

    public CommandResultTestDataCreator setClusterName(final String clusterName) {
        currentCommandResult.setClusterName(clusterName);
        return this;
    }

    public CommandResultTestDataCreator setNodeName(final String nodeName) {
        currentCommandResult.setNodeName(nodeName);
        return this;
    }

    public CommandResultTestDataCreator addMoreInfo(final String key, final String value) {
        currentCommandResult.getMoreInfo().put(key, value);
        return this;
    }

    public List<CommandResult> build() {
        return commandResults;
    }

    public CommandResultTestDataCreator setMoreInfo(final Map<String, String> moreInfo) {
        currentCommandResult.setMoreInfo(moreInfo);
        return this;
    }
}
