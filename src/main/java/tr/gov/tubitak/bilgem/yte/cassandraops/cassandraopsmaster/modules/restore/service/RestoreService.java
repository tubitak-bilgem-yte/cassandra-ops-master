package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api.BackupRelationRequester;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.CommandResultConstants;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util.RandomStringUtil;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api.PingService;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto.RestoreDTO;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore.RestoreRequest;

import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Log
@RequiredArgsConstructor
public class RestoreService {

	private static final String UNHEALTHY_NODE_ERROR = "ERROR: Could not connect to these nodes: %s";
	private static final int MAXIMUM_RESTORE_NODE_COUNT = 3;

	private final ClusterEntityRepository clusterEntityRepository;
	private final RestoreEntityRepository restoreEntityRepository;
	private final RestoreRequestSender restoreRequestSender;
	private final BackupRelationRequester backupRelationRequester;
	private final NodeEntityRepository nodeEntityRepository;
	private final PingService pingService;

	public void restore(final RestoreRequest restoreRequest) {

		List<String> unhealtyNodes = pingService.findUnhealthyNodes(restoreRequest.getClusterName());
		if (!unhealtyNodes.isEmpty()) {
			log.severe(String.format(UNHEALTHY_NODE_ERROR, String.join(", ", unhealtyNodes)));
			return;
		}
		List<String> backupDirectories = getFoldersInRequestedBackupFromNode(restoreRequest);
		String backupName = findBackupNameFromBackupPath(backupDirectories.get(0));
		String snapshotDirectories = getSnapshotDirectories(backupDirectories);
		String parentRelation = RandomStringUtil.generateAlphanumericString(CommandResultConstants.RELATION_LENGTH);

		RestoreDTO restoreDTO = new RestoreDTO();
		restoreDTO.setParentRelation(parentRelation);
		restoreDTO.setRestoreKeyspaces(restoreRequest.getRestoreKeyspaces());
		restoreDTO.setBackupName(backupName);
		restoreDTO.setClusterName(restoreRequest.getClusterName());
		restoreDTO.setAllBackupFolders(backupDirectories);

		restoreBatch(restoreDTO, snapshotDirectories);
	}

	private List<String> getFoldersInRequestedBackupFromNode(final RestoreRequest restoreRequest) {
		ClusterEntity clusterEntity = clusterEntityRepository.findByClusterName(restoreRequest.getClusterName());
		NodeEntity noodeForBackupRelationRequest = nodeEntityRepository.findByClusterName(restoreRequest.getClusterName()).get(0);
		String backupPath = Paths.get(clusterEntity.getBackupPath(),restoreRequest.getClusterName(), restoreRequest.getBackupName()).toString();
		return backupRelationRequester.requestBackupDirectories(noodeForBackupRelationRequest, backupPath);
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

	public String getSnapshotDirectories(final List<String> backupFolders) {
		Pattern pattern = Pattern.compile("(sb)[0-9]{10}");
		Matcher matcher = pattern.matcher(backupFolders.get(0));
		String firstUniqueName = "";
		if (matcher.find()) {
			firstUniqueName = matcher.group();
		}
		return firstUniqueName;
	}

	public void restoreBatch(final RestoreDTO restoreRequest, final String currentUniqueBackupName) {
		List<String> currentBatch = findNextBatch(restoreRequest.getAllBackupFolders(), currentUniqueBackupName);
		String currentBatchRelation = RandomStringUtil.generateAlphanumericString(CommandResultConstants.RELATION_LENGTH);
		restoreRequest.setBatchRelation(currentBatchRelation);

		RestoreEntity restoreEntity = restoreEntityRepository.findByParentRelation(restoreRequest.getParentRelation());
		if (restoreEntity == null) {
			restoreEntity = generateRestoreEntity(restoreRequest, currentBatch.size());
			restoreEntityRepository.save(restoreEntity);
		}
		sendRestoreRequestsToNodes(restoreRequest, currentBatch);

	}

	public List<String> findNextBatch(final List<String> unzippedBackupDirectories, final String uniqueBackupName) {
		return unzippedBackupDirectories.stream()
				.filter(path -> path.contains(uniqueBackupName))
				.collect(Collectors.toList());
	}

	private void sendRestoreRequestsToNodes(final RestoreDTO restoreRequest, final List<String> currentBatch) {
		ClusterEntity clusterEntity = clusterEntityRepository.findByClusterName(restoreRequest.getClusterName());
		List<Integer> randomlySelectedNodeNumberList = generateRandomlySelectedNodeList(clusterEntity);
		Map<Integer, List<String>> distributedBackupPaths = distributeBackupPathsToNodes(currentBatch, randomlySelectedNodeNumberList);

		for (Integer nodeNumber : randomlySelectedNodeNumberList) {
			NodeEntity randomlySelectedNodeToExecuteRestore = clusterEntity.getNodes().get(nodeNumber);
			List<String> currentNodeRestoreDirectories = distributedBackupPaths.get(nodeNumber);
			restoreRequest.setBatchBackupFolders(currentNodeRestoreDirectories);
			String response = restoreRequestSender.sendRestoreRequest(randomlySelectedNodeToExecuteRestore, restoreRequest);
			log.info(response);
		}
	}

	private List<Integer> generateRandomlySelectedNodeList(final ClusterEntity clusterEntityFromDB) {
		int totalNodeCount = clusterEntityFromDB.getNodes().size();

		IntStream range = IntStream.rangeClosed(0, totalNodeCount - 1);
		List<Integer> nodes = range.boxed().collect(Collectors.toList());
		Collections.shuffle(nodes);
		if (totalNodeCount > 3) {
			nodes = nodes.subList(0, 3);
		}
		return nodes;
	}


	public Map<Integer, List<String>> distributeBackupPathsToNodes(final List<String> currentBatch, final List<Integer> randomlySelectedNodeNumberList) {
		Map<Integer, List<String>> nodeNumberRestorePathsMap = new HashMap<>();

		for (Integer nodeNumber : randomlySelectedNodeNumberList) {
			nodeNumberRestorePathsMap.put(nodeNumber, new ArrayList<>());
		}
		for (int i = 0; i < currentBatch.size(); i++) {
			int selectedIndex = i % randomlySelectedNodeNumberList.size();
			int selectedNodeId = randomlySelectedNodeNumberList.get(selectedIndex);
			nodeNumberRestorePathsMap.get(selectedNodeId).add(currentBatch.get(i));
		}
		return nodeNumberRestorePathsMap;
	}


	private RestoreEntity generateRestoreEntity(final RestoreDTO restoreDTO, final int currentBatchSize) {
		ClusterEntity clusterEntityFromDB = clusterEntityRepository.findByClusterName(restoreDTO.getClusterName());
		short selectedNodeCount = calculateRestoreNodeCount(clusterEntityFromDB, currentBatchSize);
		String backupName = restoreDTO.getBackupName();
		String parentRelation = restoreDTO.getParentRelation();
		List<String> uniqueNames = getUniqueNames(restoreDTO.getAllBackupFolders());
		Set<String> backupPaths = new HashSet<>(restoreDTO.getAllBackupFolders());
		Set<String> restoreKeyspaces = new HashSet<>(restoreDTO.getRestoreKeyspaces());

		return new RestoreEntity(backupName, uniqueNames, backupPaths, selectedNodeCount, parentRelation, restoreKeyspaces);
	}

	private short calculateRestoreNodeCount(final ClusterEntity clusterEntityFromDB, final int batchSize) {
		int nodeCountOfCluster = clusterEntityFromDB.getNodes().size();
		int nodeLimit = (nodeCountOfCluster >= RestoreService.MAXIMUM_RESTORE_NODE_COUNT ? RestoreService.MAXIMUM_RESTORE_NODE_COUNT : nodeCountOfCluster);
		return (short) (batchSize >= nodeLimit ? nodeLimit : batchSize);
	}

	public List<String> getUniqueNames(final List<String> backupPaths) {
		List<String> uniqueNames = new ArrayList<>();
		Pattern pattern = Pattern.compile("(ib|sb)[0-9]{10}");
		for (String backupPath : backupPaths) {
			Matcher matcher = pattern.matcher(backupPath);
			String uniqueName = "";
			while (matcher.find()) {
				uniqueName = matcher.group();
			}

			if (!uniqueNames.contains(uniqueName)) {
				uniqueNames.add(uniqueName);
			}
		}
		return uniqueNames;
	}
}
