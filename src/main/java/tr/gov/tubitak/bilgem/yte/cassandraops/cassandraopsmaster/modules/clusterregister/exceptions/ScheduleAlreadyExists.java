package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions;

public class ScheduleAlreadyExists extends Throwable {
    public ScheduleAlreadyExists(final String responseMessage) {
        super(responseMessage);
    }
}
