package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.CommandResultTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api.BackupArgs;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RunWith(SpringJUnit4ClassRunner.class)
@RestClientTest(BackupRequesterClient.class)
public class BackupRequesterClientIT {
    private static final String SUCCESS_MESSAGE_TEMPLATE = "Succesfully sent backup command (relation: %s) to %s.%s";
    private static final String ERROR_MESSAGE_TEMPLATE = "Received error from nodeagent %s.%s after sending backup command (relation: %s) Status code: %s.";
    private static final String REQUEST_ADDRESS = "http://localhost:28050/backup";
    private final String commandName = "backup";
    private final String moreInfoKey = "backupLabel";

    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private BackupRequesterClient backupRequesterClient;
    private NodeEntity nodeEntity;
    private BackupArgs backupArgs;
    private CommandResult expectedCommandResult;
    private CommandResultTestDataCreator commandResultTestDataCreator;

    @Test
    public void requestBackup_sendBackupRequest_success() throws Exception {
        String serverResponseString = "OK";

        init();
        server.expect(requestTo(BackupRequesterClientIT.REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(serverResponseString, MediaType.APPLICATION_JSON));
        String message = String.format(BackupRequesterClientIT.SUCCESS_MESSAGE_TEMPLATE,
                backupArgs.getRelation(), nodeEntity.getClusterName(), nodeEntity.getNodeName());
        expectedCommandResult = commandResultTestDataCreator.addCommandResult()
                .setCommandName(commandName)
                .setRelation(backupArgs.getRelation())
                .setStatus(CommandResultConstants.CommandStatus.RUNNING)
                .setMessage(message)
                .setClusterName(nodeEntity.getClusterName())
                .setNodeName(nodeEntity.getNodeName())
                .addMoreInfo(moreInfoKey, backupArgs.getBackupLabel())
                .build().get(0);
        CommandResult actualCommandResult = backupRequesterClient.requestBackup(nodeEntity, backupArgs);
        assertThat(actualCommandResult, equalTo(expectedCommandResult));
    }

    @Test
    public void requestBackup_sendBackupRequest_throwsHttpClientErrorException() throws Exception {
        String expectedServerResponseCode = "404";

        init();
        server.expect(requestTo(BackupRequesterClientIT.REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        String message = String.format(BackupRequesterClientIT.ERROR_MESSAGE_TEMPLATE,
                nodeEntity.getClusterName(),
                nodeEntity.getNodeName(),
                backupArgs.getRelation(),
                expectedServerResponseCode);
        expectedCommandResult = commandResultTestDataCreator.addCommandResult()
                .setCommandName(commandName)
                .setRelation(backupArgs.getRelation())
                .setStatus(CommandResultConstants.CommandStatus.ERROR)
                .setMessage(message)
                .setClusterName(nodeEntity.getClusterName())
                .setNodeName(nodeEntity.getNodeName())
                .addMoreInfo(moreInfoKey, backupArgs.getBackupLabel())
                .build().get(0);
        CommandResult actualCommandResult = backupRequesterClient.requestBackup(nodeEntity, backupArgs);
        assertThat(actualCommandResult, equalTo(expectedCommandResult));
    }

    @Test
    public void requestBackup_sendBackupRequest_throwsHttpServerErrorException() throws Exception {
        String expectedServerResponseCode = "500";

        init();
        server.expect(requestTo(BackupRequesterClientIT.REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        String message = String.format(BackupRequesterClientIT.ERROR_MESSAGE_TEMPLATE,
                nodeEntity.getClusterName(),
                nodeEntity.getNodeName(),
                backupArgs.getRelation(),
                expectedServerResponseCode);
        expectedCommandResult = commandResultTestDataCreator.addCommandResult()
                .setCommandName(commandName)
                .setRelation(backupArgs.getRelation())
                .setStatus(CommandResultConstants.CommandStatus.ERROR)
                .setMessage(message)
                .setClusterName(nodeEntity.getClusterName())
                .setNodeName(nodeEntity.getNodeName())
                .addMoreInfo(moreInfoKey, backupArgs.getBackupLabel())
                .build().get(0);
        CommandResult actualCommandResult = backupRequesterClient.requestBackup(nodeEntity, backupArgs);
        assertThat(actualCommandResult, equalTo(expectedCommandResult));
    }

    public void init() {
        String relation = "abc123";
        String backupLabel = "2018-11-1";
        String ipAddress = "localhost";
        int port = 28050;
        String nodeName = "node1";
        String clusterName = "cluster1";

        backupArgs = new BackupArgs();
        NodeEntityTestDataCreator nodeEntityTestDataCreator = new NodeEntityTestDataCreator();
        expectedCommandResult = new CommandResult();
        commandResultTestDataCreator = new CommandResultTestDataCreator();
        backupArgs.setRelation(relation);
        backupArgs.setBackupLabel(backupLabel);
        nodeEntity = nodeEntityTestDataCreator.addNodeEntity()
                .setNodeEntityIpAddress(ipAddress)
                .setNodeEntityPort(port)
                .setNodeName(nodeName)
                .setClusterName(clusterName)
                .build().get(0);
    }

}