package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.exceptions;

public class BackupDoesNotExistsException extends Exception {
    public BackupDoesNotExistsException(final String message) {
        super(message);
    }

}
