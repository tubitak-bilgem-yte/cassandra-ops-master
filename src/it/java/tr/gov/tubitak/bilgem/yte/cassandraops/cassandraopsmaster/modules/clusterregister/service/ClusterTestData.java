package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.NodeEntityData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;

import java.util.List;

public class ClusterTestData {
    public static final String TEST_TEMP_BACKUP_PATH = "/data/data/backups";
    public static final String OPSMASTER_BACKUP_PATH = "/NFS/backups";
    public static final String CLUSTER_1 = "cluster1";
    public static final String CLUSTER_2 = "cluster2";
    public static final String CLUSTER_3 = "cluster3";
    public static final String UPDATED_BACKUP_PATH = "/NFS/backups-2";

    public static List<NodeEntity> createDefaultNodeData() {
        List<NodeEntity> testData = new NodeEntityTestBuilder()
                .addNode()
                .setClusterName(ClusterTestData.CLUSTER_1)
                .setNodeName(NodeEntityData.NODEAGENT1_NODE_NAME)
                .setIp(NodeEntityData.NODEAGENT1_IP)
                .setPort(NodeEntityData.NODEAGENT_PORT)
                .setTempBackupPath(ClusterTestData.TEST_TEMP_BACKUP_PATH)
                .addNode()
                .setClusterName(ClusterTestData.CLUSTER_1)
                .setNodeName(NodeEntityData.NODEAGENT2_NODE_NAME)
                .setIp(NodeEntityData.NODEAGENT2_IP)
                .setPort(NodeEntityData.NODEAGENT_PORT)
                .setTempBackupPath(ClusterTestData.TEST_TEMP_BACKUP_PATH)
                .addNode()
                .setClusterName(ClusterTestData.CLUSTER_1)
                .setNodeName(NodeEntityData.NODEAGENT3_NODE_NAME)
                .setIp(NodeEntityData.NODEAGENT3_IP)
                .setPort(NodeEntityData.NODEAGENT_PORT)
                .setTempBackupPath(ClusterTestData.TEST_TEMP_BACKUP_PATH)
                .addNode()
                .setClusterName(ClusterTestData.CLUSTER_1)
                .setNodeName(NodeEntityData.NODEAGENT4_NODE_NAME)
                .setIp(NodeEntityData.NODEAGENT4_IP)
                .setPort(NodeEntityData.NODEAGENT_PORT)
                .setTempBackupPath(ClusterTestData.TEST_TEMP_BACKUP_PATH)
                .addNode()
                .setClusterName(ClusterTestData.CLUSTER_1)
                .setNodeName(NodeEntityData.NODEAGENT5_NODE_NAME)
                .setIp(NodeEntityData.NODEAGENT5_IP)
                .setPort(NodeEntityData.NODEAGENT_PORT)
                .setTempBackupPath(ClusterTestData.TEST_TEMP_BACKUP_PATH)
                .build();
        return testData;
    }
}
