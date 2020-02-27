package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.commandresult.CommandResultHandler;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.commandresult.CommandResultRegistry;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;

@RestController
public class CommandResultReceiveController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandResultReceiveController.class);

    private CommandResultRegistry commandResultRegistry;

    @PostMapping("receiveResult")
    @ResponseStatus(HttpStatus.OK)
    public void receiveResult(@RequestBody final CommandResult result) {
        CommandResultHandler resultHandler = commandResultRegistry.getResultHandler(result.getCommandName());
        resultHandler.handle(result);
    }

    @Autowired
    protected void setCommandResultRegistry(final CommandResultRegistry commandResultRegistry) {
        this.commandResultRegistry = commandResultRegistry;
    }

    protected Logger getLogger() {
        return CommandResultReceiveController.LOGGER;
    }
}
