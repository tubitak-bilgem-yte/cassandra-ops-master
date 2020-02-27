package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api.CopyBackupRequester;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api.entity.CopyArgs;

@Component
public class CopyBackupRequesterClient implements CopyBackupRequester {
    private static final String SUCCESS_MESSAGE_TEMPLATE = "Successfully sent copy command (relation: %s) to node %s.%s.";
    private static final String ERROR_MESSAGE_TEMPLATE = "Node %s.%s gave error for copy command (relation: %s) Status code: %s.";

    private static final String REQUEST_ADDRESS_TEMPLATE = "http://%s:%d/%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyBackupRequesterClient.class);


    private RestTemplate restTemplate;

    @Override
    public CommandResult requestCopyFromNode(final NodeEntity nodeEntity, final CopyArgs copyArgs) {
        // before sending the copy request to a node, the CommandResult is created as if the request is successful
        CommandResult copyResult =
                createInitialCommandResult(nodeEntity.getClusterName(), copyArgs.getCopyRelation(), nodeEntity);
        try {
            String requestAddress = String.format(CopyBackupRequesterClient.REQUEST_ADDRESS_TEMPLATE,
                    nodeEntity.getIp(), nodeEntity.getPort(), CommandResultConstants.CommandNames.COPY);
            HttpEntity<CopyArgs> httpEntity = new HttpEntity<>(copyArgs);
            ResponseEntity<Void> responseEntity = restTemplate.exchange(requestAddress,
                    HttpMethod.POST,
                    httpEntity,
                    Void.class);
            String sentMessage = String.format(CopyBackupRequesterClient.SUCCESS_MESSAGE_TEMPLATE,
                    copyArgs.getCopyRelation(), nodeEntity.getClusterName(), nodeEntity.getNodeName());
            copyResult.setMessage(sentMessage);
            getLogger().info(sentMessage);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleError(copyResult, e, e.getStatusCode(), nodeEntity, copyArgs);
        } catch (final Exception e) {
            handleError(copyResult, e, null, nodeEntity, copyArgs);
        }
        return copyResult;
    }

    private void handleError(final CommandResult copyResult,
                             final Exception e,
                             final HttpStatus status,
                             final NodeEntity nodeEntity,
                             final CopyArgs copyArgs) {
        String errorMessage = String.format(CopyBackupRequesterClient.ERROR_MESSAGE_TEMPLATE,
                nodeEntity.getClusterName(), nodeEntity.getNodeName(), copyArgs.getCopyRelation(), status != null ? status.toString() : "NONE");
        copyResult.setStatus(CommandResultConstants.CommandStatus.ERROR);
        copyResult.setMessage(errorMessage);
        getLogger().error(copyResult.getMessage(), e);
    }

    private CommandResult createInitialCommandResult(final String clusterName, final String copyRelation, final NodeEntity nodeEntity) {
        String initialEmptyCommandResultMessage = "";
        return new CommandResult(CommandResultConstants.CommandNames.COPY,
                copyRelation,
                CommandResultConstants.CommandStatus.RUNNING,
                initialEmptyCommandResultMessage,  // CommandResult message is not specified initially.
                // It will be specified later according to the results obtained from related node.
                clusterName,
                nodeEntity.getNodeName(),
                null);
    }

    @Autowired
    protected void setRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    private Logger getLogger() {
        return CopyBackupRequesterClient.LOGGER;
    }
}
