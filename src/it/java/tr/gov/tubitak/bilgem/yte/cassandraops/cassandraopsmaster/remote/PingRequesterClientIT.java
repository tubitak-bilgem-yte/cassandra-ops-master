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
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityTestDataCreator;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.PingStatus;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@RestClientTest(PingRequesterClient.class)
public class PingRequesterClientIT {

    private final String REQUEST_ADDRESS = "http://localhost:28050/ping";
    @Autowired
    private PingRequesterClient pingRequesterClient;
    @Autowired
    private MockRestServiceServer server;
    private NodeEntityTestDataCreator testDataCreator;
    private NodeEntity testData;


    @Test
    public void pingNode_NodeIsUp_success() throws Exception {
        PingStatus serverResponseString = PingStatus.OK;

        server.expect(requestTo(REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(serverResponseString.toString(), MediaType.APPLICATION_JSON));
        init();
        testDataCreator = new NodeEntityTestDataCreator();
        PingStatus actual = pingRequesterClient.pingNode(testData);
        assertThat(actual, equalTo(serverResponseString));
    }

    @Test
    public void pingNode_NodeIsDownThrowHttpClientErrorException_fail() throws Exception {
        PingStatus serverResponseString = PingStatus.DOWN;

        server.expect(requestTo(REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        init();
        testDataCreator = new NodeEntityTestDataCreator();
        PingStatus actual = pingRequesterClient.pingNode(testData);
        assertThat(actual, equalTo(serverResponseString));
    }

    @Test
    public void pingNode_NodeIsDownThrowHttpServerErrorException_fail() throws Exception {
        PingStatus serverResponseString = PingStatus.DOWN;

        server.expect(requestTo(REQUEST_ADDRESS))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        init();
        testDataCreator = new NodeEntityTestDataCreator();
        PingStatus actual = pingRequesterClient.pingNode(testData);
        assertThat(actual, equalTo(serverResponseString));
    }

    public void init() {
        testDataCreator = new NodeEntityTestDataCreator();
        List<NodeEntity> testData = testDataCreator
                .addNodeEntity()
                .setClusterName(NodeEntityData.NODEAGENT1_CLUSTER_NAME)
                .setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
                .setNodeEntityIpAddress(NodeEntityData.LOCALHOST_IP)
                .setNodeEntityPort(NodeEntityData.NODEAGENT_PORT)
                .build();
        this.testData = testData.get(0);
    }
}
