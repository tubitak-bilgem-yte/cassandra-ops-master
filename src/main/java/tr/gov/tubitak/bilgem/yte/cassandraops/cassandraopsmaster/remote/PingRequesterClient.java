package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.PingStatus;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api.PingRequester;

@Component
public class PingRequesterClient implements PingRequester {
    private static final String ERROR_MESSAGE_TEMPLATE = "There was a problem while sending ping request to node %s.%s. Status: %s";
    private static final String SUCCESS_MESSAGE_TEMPLATE = "Successfully sent ping request to %s.%s";
    private static final String NODE_AGENT_PING_TEMPLATE = "http://%s:%d/ping";
    private static Logger LOGGER = LoggerFactory.getLogger(PingRequesterClient.class);
    private RestTemplate restTemplate;

    @Override
    public PingStatus pingNode(final NodeEntity nodeEntity) {
        PingStatus nodeStatus;
        try {
            restTemplate.getForEntity(
                    String.format(PingRequesterClient.NODE_AGENT_PING_TEMPLATE, nodeEntity.getIp(), nodeEntity.getPort()),
                    String.class);
            nodeStatus = PingStatus.OK;
            String successMessage = String.format(PingRequesterClient.SUCCESS_MESSAGE_TEMPLATE
                    , nodeEntity.getClusterName(), nodeEntity.getNodeName());
            getLogger().info(successMessage);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            nodeStatus = handleError(nodeEntity, e, e.getStatusCode());
        } catch (final Exception e) {
            nodeStatus = handleError(nodeEntity, e, null);
        }
        return nodeStatus;
    }

    private PingStatus handleError(final NodeEntity nodeEntity, final Exception e, final HttpStatus status) {
        String errorMessage = String.format(PingRequesterClient.ERROR_MESSAGE_TEMPLATE
                ,
                nodeEntity.getClusterName(),
                nodeEntity.getNodeName(),
                status != null ? status.toString() : "NONE");
        getLogger().error(errorMessage);
        return PingStatus.DOWN;
    }

    @Autowired
    protected void setRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    protected Logger getLogger() {
        return PingRequesterClient.LOGGER;
    }
}
