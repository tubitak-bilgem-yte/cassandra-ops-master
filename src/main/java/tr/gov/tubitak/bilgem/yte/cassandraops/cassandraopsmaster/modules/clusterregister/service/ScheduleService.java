package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.ClusterDoesNotExistsException;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.ScheduleAlreadyExists;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.exceptions.ScheduleDoesNotExist;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntityRepository;

import static tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.InfoMessages.*;

@Service
public class ScheduleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

    private ScheduleEntityRepository scheduleEntityRepository;
    private ClusterEntityRepository clusterEntityRepository;

    /**
     * @param clusterName Cluster that the schedule belongs
     * @return Information abaout the outcome of operation.
     * User can deactivate the schedule, meaning they may turn off the backup procedure of the given cluster,
     * so that they may adjust which clusters that they want to be backed up in a given time.
     */
    public String disableSchedule(final String clusterName) {
        String message;
        try {
            message = updateActiveStatusOnDB(clusterName, false);
        } catch (final ScheduleDoesNotExist e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * @param clusterName Cluster that the schedule belongs
     * @return Information abaout the outcome of operation.
     * User can activate the schedule, so that given cluster will be backed in given time.
     */
    public String enableSchedule(final String clusterName) {
        String message;
        try {
            message = updateActiveStatusOnDB(clusterName, true);
        } catch (final ScheduleDoesNotExist e) {
            message = e.getMessage();
        }

        return message;
    }

    private String updateActiveStatusOnDB(final String clusterName, final boolean status) throws ScheduleDoesNotExist {
        String responseMessage;
        ScheduleEntity databaseScheduleInfo = scheduleEntityRepository.findByClusterName(clusterName);
        if (databaseScheduleInfo != null) {
            databaseScheduleInfo.setIsActive(status);
            scheduleEntityRepository.save(databaseScheduleInfo);
            responseMessage = String.format(status ? SCHEDULE_ENABLE_SUCCESS_MESSAGE : SCHEDULE_DISABLE_SUCCESS_MESSAGE, clusterName);
            getLogger().info(responseMessage);
        } else {
            responseMessage = String.format(SCHEDULE_DOES_NOT_EXISTS_ERROR, clusterName);
            getLogger().error(responseMessage);
            throw new ScheduleDoesNotExist(responseMessage);
        }

        return responseMessage;
    }

    /**
     * @param clusterName
     * @throws ClusterDoesNotExistsException Each schedule should have a cluster, and this method is used for checking that.
     */
    private void clusterExistsCheck(final String clusterName) throws ClusterDoesNotExistsException {
        String responseMessage;
        if (!clusterEntityRepository.existsByClusterName(clusterName)) {
            responseMessage = String.format(CLUSTER_DOES_NOT_EXISTS_ERROR, clusterName);
            getLogger().error(responseMessage);
            throw new ClusterDoesNotExistsException(responseMessage);
        }
    }

    /**
     * @param clusterName
     * @return Information abaout the outcome of operation.
     * Schedule can be deleted completely, so that automatic backup sequence won't work on this cluster.
     */
    public String deleteSchedule(final String clusterName) {
        String message;
        try {
            message = deleteScheduleFromDB(clusterName);
        } catch (ClusterDoesNotExistsException | ScheduleDoesNotExist e) {
            message = e.getMessage();
        }

        return message;
    }

    private String deleteScheduleFromDB(final String clusterName) throws ScheduleDoesNotExist, ClusterDoesNotExistsException {
        String responseMessage;
        clusterExistsCheck(clusterName);
        ClusterEntity clusterEntity = clusterEntityRepository.findByClusterName(clusterName);
        ScheduleEntity scheduleEntity = scheduleEntityRepository.findByClusterName(clusterName);
        if (scheduleEntity != null) {
            clusterEntity.setSchedule(null);
            clusterEntityRepository.save(clusterEntity);
            scheduleEntityRepository.delete(scheduleEntity);
            responseMessage = String.format(SCHEDULE_DELETE_SUCCESS_MESSAGE, clusterName);
            getLogger().info(responseMessage);
        } else {
            responseMessage = String.format(SCHEDULE_DOES_NOT_EXISTS_ERROR, clusterName);
            getLogger().error(responseMessage);
            throw new ScheduleDoesNotExist(responseMessage);
        }

        return responseMessage;
    }

    /**
     * @param clusterName
     * @param scheduleEntity
     * @return A new cluster can be added to a given cluster, if the cluster deosn't already have a schedule.
     */
    public String addSchedule(final String clusterName, final ScheduleEntity scheduleEntity) {
        String message;
        try {
            message = addScheduleToDB(clusterName, scheduleEntity);
        } catch (ClusterDoesNotExistsException | ScheduleAlreadyExists e) {
            message = e.getMessage();
        }

        return message;
    }

    private String addScheduleToDB(final String clusterName, final ScheduleEntity scheduleEntity) throws ClusterDoesNotExistsException, ScheduleAlreadyExists {
        String responseMessage;
        clusterExistsCheck(clusterName);
        ClusterEntity clusterEntity = clusterEntityRepository.findByClusterName(clusterName);
        ScheduleEntity scheduleEntityFromDB = scheduleEntityRepository.findByClusterName(clusterName);
        if (scheduleEntityFromDB == null) {
            if (scheduleEntity.getIsActive() == null) {
                scheduleEntity.setIsActive(true);
            }
            clusterEntity.setSchedule(scheduleEntity);
            clusterEntityRepository.save(clusterEntity);
            responseMessage = String.format(SCHEDULE_ADD_SUCCESS_MESSAGE, clusterName);
            getLogger().info(responseMessage);
        } else {
            responseMessage = String.format(SCHEDULE_ALREADY_EXISTS_ERROR, clusterName);
            getLogger().error(responseMessage);
            throw new ScheduleAlreadyExists(responseMessage);
        }
        return responseMessage;
    }

    /**
     * @param clusterName    Cluster that the schedule belongs
     * @param scheduleEntity Update information about schedule
     * @return Information abaout the outcome of operation.
     * User can update the schedule info(i.e snapshot days and backup hour).
     */
    public String updateSchedule(final String clusterName, final ScheduleEntity scheduleEntity) {
        String message;
        try {
            message = updateScheduleOnDB(clusterName, scheduleEntity);
        } catch (final ScheduleDoesNotExist e) {
            message = e.getMessage();
        }
        return message;
    }

    private String updateScheduleOnDB(final String clusterName, final ScheduleEntity scheduleEntity) throws ScheduleDoesNotExist {
        String responseMessage;
        ScheduleEntity scheduleEntityFromDB = scheduleEntityRepository.findByClusterName(clusterName);
        if (scheduleEntityFromDB != null) {
            scheduleEntityFromDB = mergeDBScheduleWithUserData(scheduleEntityFromDB, scheduleEntity);
            scheduleEntityRepository.save(scheduleEntityFromDB);
            responseMessage = String.format(SCHEDULE_UPDATE_SUCCESS_MESSAGE, clusterName);
            getLogger().info(responseMessage);
        } else {
            responseMessage = String.format(SCHEDULE_DOES_NOT_EXISTS_ERROR, clusterName);
            getLogger().error(responseMessage);
            throw new ScheduleDoesNotExist(responseMessage);
        }

        return responseMessage;
    }

    private ScheduleEntity mergeDBScheduleWithUserData(final ScheduleEntity scheduleEntityFromDB, final ScheduleEntity scheduleEntityFromUser) {
        if (scheduleEntityFromUser.getSnapshotDays() != null) {
            scheduleEntityFromDB.setSnapshotDays(scheduleEntityFromUser.getSnapshotDays());
        }
        if (scheduleEntityFromUser.getIsActive() != null) {
            scheduleEntityFromDB.setIsActive(scheduleEntityFromUser.getIsActive());
        }
        if (scheduleEntityFromUser.getBackupHour() != null) {
            scheduleEntityFromDB.setBackupHour(scheduleEntityFromUser.getBackupHour());
        }

        return scheduleEntityFromDB;
    }

    @Autowired
    protected void setScheduleEntityRepository(final ScheduleEntityRepository scheduleEntityRepository) {
        this.scheduleEntityRepository = scheduleEntityRepository;
    }

    @Autowired
    protected void setClusterEntityRepository(final ClusterEntityRepository clusterEntityRepository) {
        this.clusterEntityRepository = clusterEntityRepository;
    }

    protected Logger getLogger() {
        return ScheduleService.LOGGER;
    }

}
