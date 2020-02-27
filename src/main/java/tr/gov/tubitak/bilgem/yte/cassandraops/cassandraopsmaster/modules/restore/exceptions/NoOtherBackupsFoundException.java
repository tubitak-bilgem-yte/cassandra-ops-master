package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.exceptions;

public class NoOtherBackupsFoundException extends Exception {
    public NoOtherBackupsFoundException(final String message) {
        super(message);
    }
}
