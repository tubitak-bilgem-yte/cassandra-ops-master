package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.ClusterAlreadyExistsException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.ClusterDoesNotExistsException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;

import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;

@Service
public class ClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterService.class);

    private ClusterEntityRepository clusterEntityRepository;

    /**
     * @param clusterEntity An entity object transformed from json taken from user
     * @return returns a message indicating the result of the operation
     * This method is the public interface for adding the cluster to database. Actual adding is done in helper method
     */
    public String registerCluster(final ClusterEntity clusterEntity) {
        String responseMessage;
        try {
            responseMessage = saveClusterToDB(clusterEntity);
        } catch (final ClusterAlreadyExistsException e) {
            responseMessage = e.getMessage();
        }

        return responseMessage;
    }

    /**
     * @param clusterEntity An entity object transformed from json taken from user
     * @return returns a message indicating the result of the operation
     * @throws ClusterAlreadyExistsException If cluster already exists, it throws a custom exception
     *                                       This method transforms the DTO to actual entity ClusterRegisterInfo, then sets the cluster info of each node
     *                                       and saves it to database.
     */
    private String saveClusterToDB(final ClusterEntity clusterEntity) throws ClusterAlreadyExistsException {
        String response;
        ClusterEntity databaseClusterInfo = clusterEntityRepository.findByClusterName(clusterEntity.getClusterName());
        if (databaseClusterInfo != null) {
            response = String.format(CLUSTER_ALREADY_EXISTS_ERROR, clusterEntity.getClusterName());
            getLogger().error(response);
            throw new ClusterAlreadyExistsException(response);
        }
        clusterEntityRepository.save(clusterEntity);
        response = String.format(CLUSTER_REGISTER_SUCCESS_MESSAGE, clusterEntity.getClusterName());
        getLogger().info(response);
        return response;
    }


    /**
     * @param clusterName Cluster requested from user.
     * @return Mapped DTO object, which is same with database object
     */
    public ClusterEntity getClusterInfo(final String clusterName) {
        ClusterEntity responseEntity;
        try {
            responseEntity = getClusterFromDB(clusterName);
        } catch (final ClusterDoesNotExistsException e) {
            responseEntity = null;
        }
        return responseEntity;
    }

    /**
     * @param clusterName Cluster requested from user.
     * @return DB entity object
     * @throws ClusterDoesNotExistsException If given object doesn't exists, throws custom  exception.
     */
    private ClusterEntity getClusterFromDB(final String clusterName) throws ClusterDoesNotExistsException {
        String response;
        ClusterEntity clusterEntity = clusterEntityRepository.findByClusterName(clusterName);
        if (clusterEntity != null) {
            return clusterEntity;
        } else {
            response = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, clusterName);
            getLogger().error(response);
            throw new ClusterDoesNotExistsException(response);
        }
    }

    /**
     * @param clusterEntity Contains information about new backup path
     * @return Information abaout the outcome of operation
     * Each cluster has a backup path, where it stores the backup zip files created by each node.
     * This method is user for updating that path.
     */
    public String updateBackupPathOnDB(final ClusterEntity clusterEntity) {
        String responseMessage;
        try {
            responseMessage = updateBackupPath(clusterEntity);
        } catch (final ClusterDoesNotExistsException e) {
            responseMessage = e.getMessage();
        }
        return responseMessage;
    }

    private String updateBackupPath(final ClusterEntity clusterEntity) throws ClusterDoesNotExistsException {
        String response;
        ClusterEntity databaseInfo = clusterEntityRepository.findByClusterName(clusterEntity.getClusterName());
        if (databaseInfo != null) {
            databaseInfo.setBackupPath(clusterEntity.getBackupPath());
            clusterEntityRepository.save(databaseInfo);
            response = String.format(BACKUP_PATH_UPDATE_SUCCESS_MESSAGE, clusterEntity.getClusterName());
            getLogger().info(response);
        } else {
            response = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, clusterEntity.getClusterName());
            getLogger().error(response);
            throw new ClusterDoesNotExistsException(response);
        }
        return response;
    }

    /**
     * @param clusterName Cluster name to be deleted.
     * @return Information abaout the outcome of operation.
     * Deletes the given cluster, its schedule and node infos, if exists.
     */
    public String deleteCluster(final String clusterName) {
        String responseMessage;
        try {
            responseMessage = deleteClusterFromDB(clusterName);
        } catch (final ClusterDoesNotExistsException e) {
            responseMessage = e.getMessage();

        }
        return responseMessage;
    }

    private String deleteClusterFromDB(final String clusterName) throws ClusterDoesNotExistsException {
        String response;
        ClusterEntity clusterEntity = clusterEntityRepository.findByClusterName(clusterName);
        if (clusterEntity != null) {
            clusterEntityRepository.delete(clusterEntity);
            response = String.format(DELETE_CLUSTER_SUCCESS_MESSAGE, clusterName);
            getLogger().info(response);

        } else {
            response = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, clusterName);
            getLogger().error(response);
            throw new ClusterDoesNotExistsException(response);
        }
        return response;
    }

    @Autowired
    protected void setClusterEntityRepository(final ClusterEntityRepository clusterEntityRepository) {
        this.clusterEntityRepository = clusterEntityRepository;
    }


    protected Logger getLogger() {
        return ClusterService.LOGGER;
    }
}
