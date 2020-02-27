package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.ClusterDoesNotExistsException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.NodeAlreadyExistsException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.NodeDoesNotExistsException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;

import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;

@Service
public class NodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeService.class);

    private NodeEntityRepository nodeEntityRepository;
    private ClusterEntityRepository clusterEntityRepository;

    /**
     * @param clusterName Cluster name that this node belongs to.
     * @param nodeEntity  Information for node.
     * @return Information abaout the outcome of operation.
     * Adds the given node object to database. There can be many errors during adding for that
     * @see #addNodeToDB(String, NodeEntity)
     */
    public String addNode(final String clusterName, final NodeEntity nodeEntity) {
        String message;
        try {
            message = addNodeToDB(clusterName, nodeEntity);
        } catch (IllegalArgumentException | NodeAlreadyExistsException | ClusterDoesNotExistsException e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * @param clusterName Cluster name that this node belongs to.
     * @param nodeEntity  Information for node.
     * @return Information abaout the outcome of operation.
     * @throws IllegalArgumentException      When adding a node, all of the infor about the node must be
     *                                       given(ip, port, name, tempBackupPath) since not giving even one of them messes up the whole backup and copy
     *                                       procudures.
     * @throws NodeAlreadyExistsException    If the node already exists.
     * @throws ClusterDoesNotExistsException The cluster that this node is said to be belenged doesn't exists.
     */
    private String addNodeToDB(final String clusterName, final NodeEntity nodeEntity) throws IllegalArgumentException, NodeAlreadyExistsException, ClusterDoesNotExistsException {
        String responseMessage;
        clusterExistsCheck(clusterName);
        NodeEntity databaseNodeEntity = nodeEntityRepository.findByClusterNameAndNodeName(clusterName, nodeEntity.getNodeName());
        if (databaseNodeEntity == null) {
            responseMessage = addNodeToDBIfConstraintsSatisfy(clusterName, nodeEntity);
        } else {
            responseMessage = String.format(NODE_ALREADY_EXISTS_ERROR, clusterName, nodeEntity.getNodeName());
            throw new NodeAlreadyExistsException(responseMessage);
        }
        return responseMessage;
    }

    /**
     * @param clusterName Cluster to be tested for existance
     * @throws ClusterDoesNotExistsException Thrown exception is cluster doesn't exists.
     *                                       For all of the node related operations, the cluster that node belongs MUST exists, so this check is called
     *                                       in almost every method.
     */
    private void clusterExistsCheck(final String clusterName) throws ClusterDoesNotExistsException {
        String responseMessage;
        if (!clusterEntityRepository.existsByClusterName(clusterName)) {
            responseMessage = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, clusterName);
            getLogger().error(responseMessage);
            throw new ClusterDoesNotExistsException(responseMessage);
        }
    }

    private String addNodeToDBIfConstraintsSatisfy(final String clusterName, final NodeEntity nodeEntity) throws IllegalArgumentException {
        String responseMessage;
        String notNullCheckMessage = nodeEntity.checkAllFieldsNotNull();
        if (notNullCheckMessage.equals(ALL_CONSTRAINTS_SATISFIED_FOR_NODE_RESPONSE)) {
            nodeEntityRepository.save(nodeEntity);
            responseMessage = String.format(NODE_ADD_SUCCESS_MESSAGE, nodeEntity.getNodeName(), clusterName);
            getLogger().info(responseMessage);
        } else {
            getLogger().error(notNullCheckMessage);
            throw new IllegalArgumentException(notNullCheckMessage);
        }
        return responseMessage;
    }

    /**
     * @param clusterName Cluster name that this node belongs to.
     * @param nodeName    Node to be deleted
     * @return Information abaout the outcome of operation.
     * Deletes the node from database, if the node exists.
     */
    public String deleteNode(final String clusterName, final String nodeName) {
        String message;
        try {
            message = deleteNodeFromDB(clusterName, nodeName);
        } catch (final NodeDoesNotExistsException e) {
            message = e.getMessage();
        }

        return message;
    }

    private String deleteNodeFromDB(final String clusterName, final String nodeName) throws NodeDoesNotExistsException {
        String responseMessage;
        NodeEntity databaseNodeEntity = nodeEntityRepository.findByClusterNameAndNodeName(clusterName, nodeName);
        if (databaseNodeEntity != null) {
            nodeEntityRepository.delete(databaseNodeEntity);
            responseMessage = String.format(DELETE_NODE_SUCCESS_MESSAGE, nodeName, clusterName);
            getLogger().info(responseMessage);
        } else {
            responseMessage = String.format(NODE_DOES_NOT_EXISTS, clusterName, nodeName);
            getLogger().error(responseMessage);
            throw new NodeDoesNotExistsException(responseMessage);
        }

        return responseMessage;
    }

    public String updateNode(final String clusterName, final String node, final NodeEntity nodeEntity) {
        String message;
        try {
            message = updateNodeOnDB(clusterName, node, nodeEntity);
        } catch (final NodeDoesNotExistsException e) {
            message = e.getMessage();
        }

        return message;
    }

    private String updateNodeOnDB(final String clusterName, final String nodeName, final NodeEntity nodeEntity) throws NodeDoesNotExistsException {
        String responseResponse;
        NodeEntity databaseNodeInfo = nodeEntityRepository.findByClusterNameAndNodeName(clusterName, nodeName);
        if (databaseNodeInfo != null) {
            databaseNodeInfo = mergeDBNodeWithDTO(databaseNodeInfo, nodeEntity);
            nodeEntityRepository.save(databaseNodeInfo);
            responseResponse = String.format(NODE_UPDATE_SUCCESS_MESSAGE, nodeName, clusterName);
            getLogger().info(responseResponse);
        } else {
            responseResponse = String.format(NODE_DOES_NOT_EXISTS, clusterName, nodeName);
            getLogger().error(responseResponse);
            throw new NodeDoesNotExistsException(responseResponse);
        }
        return responseResponse;
    }

    /**
     * @param nodeEntityFromDB   Node info taken from database. Changes are written on top of this entity.
     * @param nodeEntityFromUser Node entity taken from user, containing the fields to be updated
     * @return Updated NodeRegisterInfo
     * User doesn't have to give all of the fields just to update only one of them, so this mehod checks all of the fields
     * and updates the ones that are not null.
     */
    private NodeEntity mergeDBNodeWithDTO(final NodeEntity nodeEntityFromDB, final NodeEntity nodeEntityFromUser) {
        if (nodeEntityFromUser.getNodeName() != null) {
            nodeEntityFromDB.setNodeName(nodeEntityFromUser.getNodeName());
        }
        if (nodeEntityFromUser.getTempBackupPath() != null) {
            nodeEntityFromDB.setTempBackupPath(nodeEntityFromUser.getTempBackupPath());
        }
        if (nodeEntityFromUser.getIp() != null) {
            nodeEntityFromDB.setIp(nodeEntityFromUser.getIp());
        }
        if (nodeEntityFromUser.getPort() != null) {
            nodeEntityFromDB.setPort(nodeEntityFromUser.getPort());
        }

        return nodeEntityFromDB;
    }

    @Autowired
    protected void setNodeEntityRepository(final NodeEntityRepository nodeEntityRepository) {
        this.nodeEntityRepository = nodeEntityRepository;
    }

    @Autowired
    protected void setClusterEntityRepository(final ClusterEntityRepository clusterEntityRepository) {
        this.clusterEntityRepository = clusterEntityRepository;
    }

    protected Logger getLogger() {
        return NodeService.LOGGER;
    }
}
