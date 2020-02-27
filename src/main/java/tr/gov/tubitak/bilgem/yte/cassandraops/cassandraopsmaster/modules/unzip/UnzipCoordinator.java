package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.unzip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.event.CommandSuccessInAllNodesEvent;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.persistence.CommandResult;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.RandomStringUtil;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto.RestoreDTO;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service.RestoreService;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UnzipCoordinator implements ApplicationListener<CommandSuccessInAllNodesEvent> {

    private RestoreService restoreService;

    @Override
    public void onApplicationEvent(final CommandSuccessInAllNodesEvent commandSuccessInAllNodesEvent) {
        if (commandSuccessInAllNodesEvent.getCommandName().equals(CommandResultConstants.CommandNames.UNZIP)) {
            restore(commandSuccessInAllNodesEvent.getCommandResult());
        }
    }

    private void restore(final CommandResult commandResult) {
        List<String> unzippedDirectories = Arrays.asList(commandResult.getMoreInfo().get("unzippedDirectories").split("\\|"));
        List<String> keyspaces = Arrays.asList(commandResult.getMoreInfo().get("keyspaces").split("\\|"));
        String backupName = findBackupNameFromBackupPath(unzippedDirectories.get(0));
        String firstUniqueBackupName = getFirstUniqueBackupName(unzippedDirectories);
        String parentRelation = RandomStringUtil.generateAlphanumericString(CommandResultConstants.RELATION_LENGTH);

        RestoreDTO restoreDTO = new RestoreDTO();
        restoreDTO.setParentRelation(parentRelation);
        restoreDTO.setRestoreKeyspaces(keyspaces);
        restoreDTO.setBackupName(backupName);
        restoreDTO.setClusterName(commandResult.getClusterName());
        restoreDTO.setNodeName(commandResult.getNodeName());
        restoreDTO.setAllBackupFolders(unzippedDirectories);

        restoreService.restoreBatch(restoreDTO, firstUniqueBackupName);
    }

    public String findBackupNameFromBackupPath(final String backupPath) {
        Pattern pattern = Pattern.compile("(backup)[0-9]{4}");
        Matcher matcher = pattern.matcher(backupPath);
        String backupName = "";
        if (matcher.find()) {
            backupName = matcher.group();
        }
        return backupName;
    }

    public String getFirstUniqueBackupName(final List<String> unzippedBackupDirectories) {
        Pattern pattern = Pattern.compile("(ib|sb)[0-9]{10}");
        Matcher matcher = pattern.matcher(unzippedBackupDirectories.get(0));
        String firstUniqueName = "";
        if (matcher.find()) {
            firstUniqueName = matcher.group();
        }
        return firstUniqueName;
    }

    @Autowired
    public void setRestoreService(final RestoreService restoreService) {
        this.restoreService = restoreService;
    }
}
