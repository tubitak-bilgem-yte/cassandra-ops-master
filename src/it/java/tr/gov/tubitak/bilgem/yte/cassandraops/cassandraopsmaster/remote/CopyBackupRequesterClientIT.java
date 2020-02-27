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
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api.entity.CopyArgs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@RestClientTest(CopyBackupRequesterClient.class)
public class CopyBackupRequesterClientIT {
    private static final String SUCCESS_MESSAGE_TEMPLATE = "Successfully sent copy command (relation: %s) to node %s.%s.";
    private static final String ERROR_MESSAGE_TEMPLATE = "Node %s.%s gave error for copy command (relation: %s) Status code: %s.";
    private static final String REQUEST_ADDRESS = "http://localhost:28060/copy";
    private final String commandName = "copy";

    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private CopyBackupRequesterClient copyBackupRequesterClient;
    private NodeEntity nodeEntity;
    private CopyArgs copyArgs;
    private CommandResult expectedCommandResult;
    private CommandResultTestDataCreator commandResultTestDataCreator;

    @Test
    public void requestCopyFromNode_sendCopyRequest_success() throws Exception {
        String serverResponseString = "OK";

        init();
        server.expect(requestTo(CopyBackupRequesterClientIT.REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(serverResponseString, MediaType.APPLICATION_JSON));
        String sentMessage = String.format(CopyBackupRequesterClientIT.SUCCESS_MESSAGE_TEMPLATE,
                copyArgs.getCopyRelation(), nodeEntity.getClusterName(), nodeEntity.getNodeName());
        expectedCommandResult = commandResultTestDataCreator.addCommandResult()
                .setCommandName(commandName)
                .setRelation(copyArgs.getCopyRelation())
                .setStatus(CommandResultConstants.CommandStatus.RUNNING)
                .setMessage(sentMessage)
                .setClusterName(nodeEntity.getClusterName())
                .setNodeName(nodeEntity.getNodeName())
                .setMoreInfo(null)
                .build().get(0);
        CommandResult actualCommandResult = copyBackupRequesterClient.requestCopyFromNode(nodeEntity, copyArgs);
        assertThat(actualCommandResult, equalTo(expectedCommandResult));
    }

    @Test
    public void requestCopyFromNode_sendCopyRequest_throwsHttpClientErrorException() throws Exception {
        String serverResponseCode = "404";

        init();
        server.expect(requestTo(CopyBackupRequesterClientIT.REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        String errorMessage = String.format(CopyBackupRequesterClientIT.ERROR_MESSAGE_TEMPLATE,
                nodeEntity.getClusterName(), nodeEntity.getNodeName(), copyArgs.getCopyRelation(), serverResponseCode);
        expectedCommandResult = commandResultTestDataCreator.addCommandResult()
                .setCommandName(commandName)
                .setRelation(copyArgs.getCopyRelation())
                .setStatus(CommandResultConstants.CommandStatus.ERROR)
                .setMessage(errorMessage)
                .setClusterName(nodeEntity.getClusterName())
                .setNodeName(nodeEntity.getNodeName())
                .setMoreInfo(null)
                .build().get(0);
        CommandResult actualCommandResult = copyBackupRequesterClient.requestCopyFromNode(nodeEntity, copyArgs);
        assertThat(actualCommandResult, equalTo(expectedCommandResult));
    }

    @Test
    public void requestCopyFromNode_sendCopyRequest_throwsHttpServerErrorException() throws Exception {
        String serverResponseCode = "500";

        init();
        server.expect(requestTo(CopyBackupRequesterClientIT.REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        String errorMessage = String.format(CopyBackupRequesterClientIT.ERROR_MESSAGE_TEMPLATE,
                nodeEntity.getClusterName(), nodeEntity.getNodeName(), copyArgs.getCopyRelation(), serverResponseCode);
        expectedCommandResult = commandResultTestDataCreator.addCommandResult()
                .setCommandName(commandName)
                .setRelation(copyArgs.getCopyRelation())
                .setStatus(CommandResultConstants.CommandStatus.ERROR)
                .setMessage(errorMessage)
                .setClusterName(nodeEntity.getClusterName())
                .setNodeName(nodeEntity.getNodeName())
                .setMoreInfo(null)
                .build().get(0);
        CommandResult actualCommandResult = copyBackupRequesterClient.requestCopyFromNode(nodeEntity, copyArgs);
        assertThat(actualCommandResult, equalTo(expectedCommandResult));
    }


    public void init() {
        String sourcePath = "/";
        String destinationPath = "/backups";
        String relation = "abc123";
        String ipAddress = "localhost";
        String nodeName = "node1";
        String clusterName = "cluster1";
        boolean deleteSourceAfterCopy = false;
        int port = 28060;

        copyArgs = new CopyArgs(sourcePath, destinationPath, relation, deleteSourceAfterCopy);
        NodeEntityTestDataCreator nodeEntityTestDataCreator = new NodeEntityTestDataCreator();
        expectedCommandResult = new CommandResult();
        commandResultTestDataCreator = new CommandResultTestDataCreator();
        nodeEntity = nodeEntityTestDataCreator.addNodeEntity()
                .setNodeEntityIpAddress(ipAddress)
                .setNodeEntityPort(port)
                .setNodeName(nodeName)
                .setClusterName(clusterName)
                .build().get(0);
    }

}