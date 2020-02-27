package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions;

public class NodeDoesNotExistsException extends Throwable {
    public NodeDoesNotExistsException(final String responseMessage) {
        super(responseMessage);
    }
}
