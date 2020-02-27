package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup;


import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;

import java.util.ArrayList;
import java.util.List;

public class NodeEntityTestDataCreator {

    private List<NodeEntity> nodeEntityList;
    private NodeEntity currentNodeEntity;

    public NodeEntityTestDataCreator() {
        nodeEntityList = new ArrayList<>();
    }

    public NodeEntityTestDataCreator addNodeEntity() {
        currentNodeEntity = new NodeEntity();
        nodeEntityList.add(currentNodeEntity);
        return this;
    }

    public NodeEntityTestDataCreator setClusterName(final String clusterName) {
        currentNodeEntity.setClusterName(clusterName);
        return this;
    }

    public NodeEntityTestDataCreator setNodeName(final String nodeName) {
        currentNodeEntity.setNodeName(nodeName);
        return this;
    }

    public NodeEntityTestDataCreator setNodeEntityIpAddress(final String ipAddress) {
        currentNodeEntity.setIp(ipAddress);
        return this;
    }

    public NodeEntityTestDataCreator setNodeEntityPort(final int port) {
        currentNodeEntity.setPort(port);
        return this;
    }

    public List<NodeEntity> build() {
        //TODO:add the nodeEntitys to test database
        return nodeEntityList;
    }

}
