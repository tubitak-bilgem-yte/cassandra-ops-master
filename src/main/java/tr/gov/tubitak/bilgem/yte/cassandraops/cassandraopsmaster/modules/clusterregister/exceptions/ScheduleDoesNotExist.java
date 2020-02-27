package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions;

public class ScheduleDoesNotExist extends Throwable {
    public ScheduleDoesNotExist(final String responseMessage) {
        super(responseMessage);
    }
}
