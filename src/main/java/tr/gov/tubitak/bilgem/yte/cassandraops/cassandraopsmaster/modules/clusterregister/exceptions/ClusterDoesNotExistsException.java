package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions;

public class ClusterDoesNotExistsException extends Exception {
    public ClusterDoesNotExistsException(final String message) {
        super(message);
    }
}
