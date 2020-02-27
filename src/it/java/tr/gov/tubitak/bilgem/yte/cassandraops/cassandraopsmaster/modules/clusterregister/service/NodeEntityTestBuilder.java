package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;

import java.util.ArrayList;
import java.util.List;

public class NodeEntityTestBuilder {
    private List<NodeEntity> testDataEntityList = new ArrayList<>();
    private NodeEntity currentEntity;

    public NodeEntityTestBuilder addNode() {
        currentEntity = new NodeEntity();
        testDataEntityList.add(currentEntity);
        return this;
    }

    public NodeEntityTestBuilder setClusterName(final String clusterName) {
        currentEntity.setClusterName(clusterName);
        return this;
    }

    public NodeEntityTestBuilder setNodeName(final String nodeName) {
        currentEntity.setNodeName(nodeName);
        return this;
    }

    public NodeEntityTestBuilder setTempBackupPath(final String tempBackupPath) {
        currentEntity.setTempBackupPath(tempBackupPath);
        return this;
    }

    public NodeEntityTestBuilder setIp(final String ip) {
        currentEntity.setIp(ip);
        return this;
    }

    public NodeEntityTestBuilder setPort(final int port) {
        currentEntity.setPort(port);
        return this;
    }

    public List<NodeEntity> build() {
        return this.testDataEntityList;
    }
}
