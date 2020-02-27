package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util;

public class CommandResultConstants {
    public static final int RELATION_LENGTH = 6;

    public static class MoreInfoNames {
        public static final String BACKUP_FILE_PATH = "backupFilePath";
        public static final String BACKUP_SIZE = "backupSize";
        public static final String BACKUP_LABEL = "backupLabel";
        public static final String BACKUP_TYPE = "backupType";
    }

    public static class CommandNames {
        public static final String BACKUP = "backup";
        public static final String COPY = "copy";
        public static final String RESTORE = "restore";
        public static final String UNZIP = "unzip";
        public static final String BACKUPRELATIONS = "backupRelations";
        public static final String BACKUP_FOLDERS = "backupDirectories";

    }

    public static class CommandStatus {
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
        public static final String RUNNING = "running";
    }
}


