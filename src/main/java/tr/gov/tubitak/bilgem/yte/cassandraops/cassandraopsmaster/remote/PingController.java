package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.PingStatus;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api.PingService;

import java.util.Map;

@RestController
public class PingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingController.class);
    private static final String PING_SERVICE_CALLED_MESSAGE = "Ping service called.";
    private PingService pingService;

    @GetMapping("/{cluster}/ping")
    public Map<String, PingStatus> getStatus(@PathVariable final String clusterName) {
        getLogger().info(PingController.PING_SERVICE_CALLED_MESSAGE);
        return pingService.pingNodes(clusterName);
    }

    @Autowired
    protected void setPingService(final PingService pingService) {
        this.pingService = pingService;
    }

    protected Logger getLogger() {
        return PingController.LOGGER;
    }
}
