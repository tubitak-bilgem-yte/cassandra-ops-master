package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister;

public class InfoMessages {

    public static final String CLUSTER_DOES_NOT_EXISTS_ERROR = "Cluster with name %s doesn't exists!";
    public static final String CLUSTER_ALREADY_EXISTS_ERROR = "Cluster with cluster name %s already exists!";
    public static final String BACKUP_PATH_UPDATE_SUCCESS_MESSAGE = "Successfully updated backup path for cluster %s!";
    public static final String DELETE_CLUSTER_SUCCESS_MESSAGE = "Successfully deleted cluster with cluster name %s!";
    public static final String CLUSTER_REGISTER_SUCCESS_MESSAGE = "Successfully registered cluster with cluster name %s!";

    public static final String NODE_ALREADY_EXISTS_ERROR = "Node with cluster name %s and node name %s already exists!";
    public static final String NODE_ADD_SUCCESS_MESSAGE = "Succesfully added node %s to cluster %s!";
    public static final String DELETE_NODE_SUCCESS_MESSAGE = "Successfully deleted %s from cluster %s";
    public static final String NODE_UPDATE_SUCCESS_MESSAGE = "Successfully updated node %s with cluster %s";
    public static final String ALL_CONSTRAINTS_SATISFIED_FOR_NODE_RESPONSE = "OK";
    public static final String NODE_DOES_NOT_EXISTS = "Node with cluster name %s and node name with %s does not exists";

    public static final String SCHEDULE_DISABLE_SUCCESS_MESSAGE = "Schedule for cluster %s disabled.";
    public static final String SCHEDULE_ENABLE_SUCCESS_MESSAGE = "Schedule for cluster %s enabled.";
    public static final String SCHEDULE_DOES_NOT_EXISTS_ERROR = "Schedule for cluster %s does not exists!";
    public static final String SCHEDULE_DELETE_SUCCESS_MESSAGE = "Schedule for cluster %s deleted succesfully.";
    public static final String SCHEDULE_ADD_SUCCESS_MESSAGE = "Schedule for cluster %s added successfully.";
    public static final String SCHEDULE_ALREADY_EXISTS_ERROR = "Schedule for cluster %s already exists!";
    public static final String SCHEDULE_UPDATE_SUCCESS_MESSAGE = "Schedule for cluster %s updated successfully!";

    public static final String CLUSTER_DOES_NOT_HAVE_ANY_NODES_ERROR = "Cluster with name %s does not have any nodes!";
    public static final String BACKUP_FILE_DOES_NOT_EXISTS = "Backup named %s does not exists for cluster %s! Path: %s";
}
