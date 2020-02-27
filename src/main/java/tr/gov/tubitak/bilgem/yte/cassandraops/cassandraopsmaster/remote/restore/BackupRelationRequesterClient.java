package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api.BackupRelationRequester;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.BackupRequesterClient;

import java.util.List;

import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants.CommandNames.BACKUP_FOLDERS;

@Component
@RequiredArgsConstructor
public class BackupRelationRequesterClient implements BackupRelationRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupRequesterClient.class);
    private static final String REQUEST_ADDRESS_TEMPLATE = "http://%s:%d/%s";
    private final RestTemplate restTemplate;

    @Override
    public List<String> requestBackupRelations(final NodeEntity nodeEntity, final String path) {

        String requestAddress = String.format(BackupRelationRequesterClient.REQUEST_ADDRESS_TEMPLATE,
                nodeEntity.getIp(), nodeEntity.getPort(), CommandResultConstants.CommandNames.BACKUPRELATIONS);
        HttpEntity<String> backupRelationsRequest = new HttpEntity<>(path);
        ResponseEntity<List> response = restTemplate.exchange(requestAddress, HttpMethod.POST, backupRelationsRequest, List.class);
        return response.getBody();

    }

    @Override
    public List<String> requestBackupDirectories(NodeEntity nodeEntity, String path) {
        String baseRequestAdress = String.format(REQUEST_ADDRESS_TEMPLATE, nodeEntity.getIp(), nodeEntity.getPort(),BACKUP_FOLDERS);
        UriComponents uri =  UriComponentsBuilder.fromUriString(baseRequestAdress)
                .queryParam("backupPath", path)
                .build();

       return (List<String >) restTemplate.getForObject(uri.toUri(),List.class);
    }

    protected Logger getLogger() {
        return BackupRelationRequesterClient.LOGGER;
    }
}
