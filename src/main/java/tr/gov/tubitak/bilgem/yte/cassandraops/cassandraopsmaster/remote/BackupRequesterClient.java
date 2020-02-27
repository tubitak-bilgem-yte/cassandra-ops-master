package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api.BackupArgs;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api.BackupRequester;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;

import java.util.HashMap;
import java.util.Map;

@Component
public class BackupRequesterClient implements BackupRequester {
	private static final Logger LOGGER = LoggerFactory.getLogger(BackupRequesterClient.class);
	private static final String REQUEST_ADDRESS_TEMPLATE = "http://%s:%d/%s";
	private static final String ERROR_MESSAGE_TEMPLATE = "Received error from nodeagent %s.%s after sending backup command (relation: %s) Status code: %s.";
	private static final String SUCCESS_MESSAGE_TEMPLATE = "Succesfully sent backup command (relation: %s) to %s.%s";

	private RestTemplate restTemplate;

    /*
        Sends a backup request to given node, with given backup arguements.
     */

	@Override
	public CommandResult requestBackup(final NodeEntity nodeEntity, final BackupArgs backupArgs) {
		HttpEntity<BackupArgs> backupRequest = new HttpEntity<>(backupArgs);
		String requestAddress = String.format(BackupRequesterClient.REQUEST_ADDRESS_TEMPLATE,
				nodeEntity.getIp(), nodeEntity.getPort(), CommandResultConstants.CommandNames.BACKUP);
		CommandResult commandResult;
		try {
			restTemplate.exchange(requestAddress,
					HttpMethod.POST, backupRequest,
					Void.class);
			commandResult = handleSuccess(nodeEntity, backupArgs);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			commandResult = handleError(nodeEntity, backupArgs, e.getStatusCode());
		}
		return commandResult;
	}

	private CommandResult handleError(final NodeEntity nodeEntity, final BackupArgs backupArgs, final HttpStatus statusCode) {
		String message = String.format(BackupRequesterClient.ERROR_MESSAGE_TEMPLATE,
				nodeEntity.getClusterName(),
				nodeEntity.getNodeName(),
				backupArgs.getRelation(),
				statusCode != null ? statusCode : "NONE");
		getLogger().error(message);
		return createCommandResult(backupArgs.getRelation(),
				CommandResultConstants.CommandStatus.ERROR,
				message,
				nodeEntity.getClusterName(),
				nodeEntity.getNodeName(),
				backupArgs.getBackupLabel());
	}

	private CommandResult handleSuccess(final NodeEntity nodeEntity, final BackupArgs backupArgs) {
		String message = String.format(BackupRequesterClient.SUCCESS_MESSAGE_TEMPLATE,
				backupArgs.getRelation(), nodeEntity.getClusterName(), nodeEntity.getNodeName());
		getLogger().info(message);
		return createCommandResult(backupArgs.getRelation(),
				CommandResultConstants.CommandStatus.RUNNING,
				message,
				nodeEntity.getClusterName(),
				nodeEntity.getNodeName(),
				backupArgs.getBackupLabel());
	}

	private CommandResult createCommandResult(final String relation,
											  final String status,
											  final String message,
											  final String clusterName,
											  final String nodeName,
											  final String backupLabel) {
		Map<String, String> moreInfo = new HashMap<>();
		moreInfo.put(CommandResultConstants.MoreInfoNames.BACKUP_LABEL, backupLabel);
		return new CommandResult(CommandResultConstants.CommandNames.BACKUP,
				relation, status, message, clusterName, nodeName, moreInfo);
	}

	private Logger getLogger() {
		return BackupRequesterClient.LOGGER;
	}

	@Autowired
	protected void setRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}
}
