package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.commandresult.CommandResultHandler;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;

@Service
public class CopyResultHandler extends CommandResultHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(CopyResultHandler.class);

    @Override
    public String getCommandName() {
        return CommandResultConstants.CommandNames.COPY;
    }

    @Override
    protected Logger getLogger() {
        return CopyResultHandler.LOGGER;
    }
}
