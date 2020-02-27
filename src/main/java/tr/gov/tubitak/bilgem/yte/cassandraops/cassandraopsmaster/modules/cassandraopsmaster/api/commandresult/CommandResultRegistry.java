package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.commandresult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandResultRegistry {
    private final Map<String, CommandResultHandler> resultHandlers = new HashMap<>();

    @Autowired
    public CommandResultRegistry(final List<CommandResultHandler> resultHandlerList) {
        for (CommandResultHandler handler : resultHandlerList) {
            resultHandlers.put(handler.getCommandName(), handler);
        }
    }

    public CommandResultHandler getResultHandler(final String commandName) {
        return resultHandlers.get(commandName);
    }
}
