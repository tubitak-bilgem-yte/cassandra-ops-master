package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.exceptions;

public class ClusterDoesNotHaveAnyNodesException extends Exception {
    public ClusterDoesNotHaveAnyNodesException(final String message) {
        super(message);
    }

}
