package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto.RestoreDTO;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service.RestoreRequestSender;

@Component
public class RestoreRequestSenderClient implements RestoreRequestSender {

    private static final String REQUEST_ADDRESS_TEMPLATE = "http://%s:%d/%s";
    private static final String SUCCESSFULL_REQUEST = "Successfully sent restoreBatch request %s to cluster %s";
    private static final String REQUEST_FAIL_ERROR = "Failed to send the restoreBatch request to cluster %s to URL %s. Error message: %s";
    private Logger logger = LoggerFactory.getLogger(RestoreRequestSenderClient.class);
    private RestTemplate restTemplate;

    @Override
    public String sendRestoreRequest(final NodeEntity nodeEntity, final RestoreDTO restoreDTO) {
        restoreDTO.setNodeName(nodeEntity.getNodeName());
        restoreDTO.setClusterName(nodeEntity.getClusterName());
        HttpEntity<RestoreDTO> backupRequest = new HttpEntity<>(restoreDTO);
        String responseMessage;
        String requestAddress = String.format(RestoreRequestSenderClient.REQUEST_ADDRESS_TEMPLATE, nodeEntity.getIp(), nodeEntity.getPort(), CommandResultConstants.CommandNames.RESTORE);
        try {
            restTemplate.exchange(requestAddress, HttpMethod.POST, backupRequest, Void.class);
        } catch (final HttpClientErrorException | HttpServerErrorException e) {
            System.out.println(e.getMessage());
            responseMessage = String.format(RestoreRequestSenderClient.REQUEST_FAIL_ERROR, nodeEntity.getClusterName(), requestAddress, e.getMessage());
            return responseMessage;
        }
        responseMessage = String.format(RestoreRequestSenderClient.SUCCESSFULL_REQUEST, restoreDTO.getBatchRelation(), nodeEntity.getClusterName());
        return responseMessage;
    }

    @Autowired
    protected void setRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    protected Logger getLogger() {
        return logger;
    }
}
