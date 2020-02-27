package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions;

public class ClusterAlreadyExistsException extends Exception {

    public ClusterAlreadyExistsException(final String message) {
        super(message);
    }
}
