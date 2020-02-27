package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.entity.BackupType;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResultRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.EnvironmentUtil;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.RandomStringUtil;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.*;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api.PingService;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class BackupCoordinator {
    private static final String UNHEALTHY_NODE_ERROR = "ERROR: Could not connect to these nodes: %s";
    private static final String BACKUP_LABEL_FORMAT = "b%s_%s_%s";  // btimestamp_relation_nodeName
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupCoordinator.class);
    private NodeEntityRepository nodeEntityRepository;
    private CommandResultRepository commandResultRepository;
    private EnvironmentUtil environmentUtil;
    private BackupRequester backupRequester;
    private PingService pingService;
    private ScheduleEntityRepository scheduleEntityRepository;
    private ClusterEntityRepository clusterEntityRepository;

    private int dayOfMonth;
    private String currentTime;

    /*
        This function is called at 2:00 AM each night, to take either incremental or snapshot backup. It checks the current
        day and if it is in a snapshot day (default is 1st and 16th of each month), it takes a snapshot. Otherwise, it takes
        an incremental backup
     */
    @Scheduled(cron = "0 0,18 * ? * *")
    public void scheduleBackup() {
        // determine if this is snapshot day or incremental backup day
        List<ScheduleEntity> scheduleEntityList = scheduleEntityRepository.findAll();
        fillTodaysDateInfo();
        for (ScheduleEntity scheduleEntity : scheduleEntityList) {
            if (scheduleEntity.getBackupHour().equals(currentTime) && scheduleEntity.getIsActive()) {
                String backupType = BackupType.INCREMENTAL_BACKUP;
                if (scheduleEntity.getSnapshotDays().contains(dayOfMonth)) {
                    backupType = BackupType.SNAPSHOT;
                }
                requestBackup(backupType, scheduleEntity.getClusterName());
            }
        }
    }

    public void fillTodaysDateInfo() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        currentTime = LocalTime.now().format(dateTimeFormatter);
        dayOfMonth = LocalDate.now().getDayOfMonth();
    }

    @Autowired
    protected void setEnvironmentUtil(final EnvironmentUtil environmentUtil) {
        this.environmentUtil = environmentUtil;
    }

    /*
        Sends backup request to each node. The nodes that should be sent the request are taken from database, which have been saved
        there before through registration process.
     */
    public String requestBackup(final String backupType, final String clusterName) {
        List<String> unhealtyNodes = pingService.findUnhealthyNodes(clusterName);
        if (!unhealtyNodes.isEmpty()) {
            return String.format(BackupCoordinator.UNHEALTHY_NODE_ERROR, String.join(", ", unhealtyNodes));
        }
        // relation is used to give a common attribute for this snapshot between all nodes.
        String relation = RandomStringUtil.generateAlphanumericString(CommandResultConstants.RELATION_LENGTH);
        String backupTimestamp = generateBackupTimestamp();
        ClusterEntity clusterEntity = clusterEntityRepository.findByClusterName(clusterName);
        List<String> keyspacesToBeBackedUp = Arrays.asList(clusterEntity.getKeyspaces().split(","));
        for (NodeEntity nodeEntity : nodeEntityRepository.findByClusterName(clusterName)) {
            String backupLabel = String.format(BackupCoordinator.BACKUP_LABEL_FORMAT,
                    backupTimestamp,
                    relation,
                    nodeEntity.getNodeName());
            BackupArgs backupArgs = createBackupArgs(nodeEntity, relation, backupType, backupLabel, keyspacesToBeBackedUp);
            CommandResult commandResult = backupRequester.requestBackup(nodeEntity, backupArgs);
            commandResultRepository.save(commandResult);
        }
        return "OK";
    }


    private String generateBackupTimestamp() {
        String pattern = "yyMMddHHmm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    private BackupArgs createBackupArgs(final NodeEntity nodeEntity, final String relation, final String backupType, final String backupLabel, final List<String> keyspaces) {
        BackupArgs args = new BackupArgs();
        args.setCassandraDataPath(environmentUtil.getCassandraDataPath());
        args.setTempBackupPath(nodeEntity.getTempBackupPath());
        args.setKeyspaces(keyspaces);
        args.setRelation(relation);
        args.setBackupLabel(backupLabel);
        args.setBackupType(backupType);
        //TODO: Needs changing when writing react front-end
        args.setCleanOldBackups(environmentUtil.getCleanOldBackups());
        return args;
    }

    @Autowired
    protected void setNodeEntityRepository(final NodeEntityRepository nodeEntityRepository) {
        this.nodeEntityRepository = nodeEntityRepository;
    }

    @Autowired
    protected void setCommandResultRepository(final CommandResultRepository commandResultRepository) {
        this.commandResultRepository = commandResultRepository;
    }

    @Autowired
    protected void setBackupRequester(final BackupRequester backupRequester) {
        this.backupRequester = backupRequester;
    }

    @Autowired
    protected void setPingService(final PingService pingService) {
        this.pingService = pingService;
    }

    @Autowired
    protected void setScheduleEntityRepository(final ScheduleEntityRepository scheduleEntityRepository) {
        this.scheduleEntityRepository = scheduleEntityRepository;
    }

    @Autowired
    public void setClusterEntityRepository(final ClusterEntityRepository clusterEntityRepository) {
        this.clusterEntityRepository = clusterEntityRepository;
    }

    protected Logger getLogger() {
        return BackupCoordinator.LOGGER;
    }

    public void setDayOfMonth(final int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setCurrentTime(final String currentTime) {
        this.currentTime = currentTime;
    }

}
