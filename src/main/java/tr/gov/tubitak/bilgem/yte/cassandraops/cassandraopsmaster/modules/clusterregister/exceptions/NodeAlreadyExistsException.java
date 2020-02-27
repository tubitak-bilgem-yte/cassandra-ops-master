package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions;

public class NodeAlreadyExistsException extends Exception {
    public NodeAlreadyExistsException(final String responseMessage) {
        super(responseMessage);
    }
}
