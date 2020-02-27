package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.impl;

import java.nio.file.Paths;

import static java.util.UUID.randomUUID;

public class CopyBackupsTestData {
    public static final String ROOT_FOLDER = Paths.get(System.getProperty("user.home"), "backups", randomUUID().toString()).toString();
    public static final String CLUSTER_DIRECTORY = Paths.get(CopyBackupsTestData.ROOT_FOLDER, "backups").toString();
    public static final String BACKUP_FOLDER_FOR_CLUSTER = Paths.get(CopyBackupsTestData.CLUSTER_DIRECTORY, "cluster1").toString();
    public static final String BACKUP1_FOLDER = Paths.get(CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER, "backups001").toString();
    public static final String BACKUP2_FOLDER = Paths.get(CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER, "backups002").toString();
    public static final String BACKUP3_FOLDER = Paths.get(CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER, "backups003").toString();
    public static final String BACKUP4_FOLDER = Paths.get(CopyBackupsTestData.BACKUP_FOLDER_FOR_CLUSTER, "backups004").toString();

    public static final String BACKUP1_FILE1 = Paths.get(CopyBackupsTestData.BACKUP1_FOLDER, "backup1").toString();
    public static final String BACKUP1_FILE2 = Paths.get(CopyBackupsTestData.BACKUP1_FOLDER, "backup2").toString();
    public static final String BACKUP2_FILE1 = Paths.get(CopyBackupsTestData.BACKUP2_FOLDER, "backup1").toString();
    public static final String BACKUP2_FILE2 = Paths.get(CopyBackupsTestData.BACKUP2_FOLDER, "backup2").toString();
    public static final String BACKUP3_FILE1 = Paths.get(CopyBackupsTestData.BACKUP3_FOLDER, "backup1").toString();
    public static final String BACKUP4_FILE1 = Paths.get(CopyBackupsTestData.BACKUP4_FOLDER, "backup1").toString();


}
