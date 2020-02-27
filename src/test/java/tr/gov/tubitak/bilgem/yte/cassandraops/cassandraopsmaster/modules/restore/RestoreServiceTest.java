package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.backup.api.BackupRelationRequester;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterTestData;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.ping.api.PingService;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.dto.RestoreDTO;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.persistance.RestoreEntityRepository;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service.RestoreRequestSender;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service.RestoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestoreServiceTest {
	private RestoreDTO restoreRequest;
	@Mock
	private ClusterEntityRepository clusterEntityRepository;
	@Mock
	private RestoreEntityRepository restoreEntityRepository;
	@Mock
	private RestoreRequestSender restoreRequestSender;

	@InjectMocks
	private RestoreService restoreService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		restoreRequest = new RestoreDTO();
		restoreRequest.setClusterName(ClusterTestData.CLUSTER_1);
		restoreRequest.setBackupName("backup0001");
		restoreRequest.setParentRelation("parent1");
		restoreRequest.setAllBackupFolders(generateAllBackupFoldersList());
		restoreRequest.setRestoreKeyspaces(generateKeyspaces());
	}

	@Test
	public void restore_BatchSizeGreaterThan3_sends3RestoreRequests() {
		ClusterEntity clusterEntity = generateClusterEntity();
		when(restoreEntityRepository.findByParentRelation(restoreRequest.getParentRelation())).thenReturn(null);
		when(clusterEntityRepository.findByClusterName(any())).thenReturn(clusterEntity);
		restoreService.restoreBatch(restoreRequest, "sb1902250953");
		Mockito.verify(restoreRequestSender, Mockito.times(3)).sendRestoreRequest(Mockito.any(), Mockito.any());

	}

	@Test
	public void restore_BatchSizeNotGreaterThan3_sendsNodeCountRestoreRequests() {
		ClusterEntity clusterEntity = generateClusterEntity();
		clusterEntity.setNodes(clusterEntity.getNodes().subList(0, 2));
		when(restoreEntityRepository.findByParentRelation(restoreRequest.getParentRelation())).thenReturn(null);
		when(clusterEntityRepository.findByClusterName(any())).thenReturn(clusterEntity);
		restoreService.restoreBatch(restoreRequest, "sb1902250953");

		Mockito.verify(restoreRequestSender, Mockito.times(clusterEntity.getNodes().size())).sendRestoreRequest(Mockito.any(), Mockito.any());

	}

	@Test
	public void getUniqueNames_getUniqueNamesFromAllFileList_success() {
		List<String> expectedUniqueNameList = new ArrayList<>();
		expectedUniqueNameList.add("sb1902250953");
		expectedUniqueNameList.add("sb1902251053");
		expectedUniqueNameList.add("ib1902251153");
		expectedUniqueNameList.add("ib1902250953");
		expectedUniqueNameList.add("ib1902251053");
		List<String> actualList = restoreService.getUniqueNames(restoreRequest.getAllBackupFolders());
		assertThat(actualList, equalTo(expectedUniqueNameList));

	}

	@Test
	public void findNextBatch_uniqueNameWasFoundInList_success() {
		String uniqueName = "ib1902250953";
		List<String> unzippedDirectories = restoreRequest.getAllBackupFolders();
		List<String> expectedBatchPathList = new ArrayList<>();
		expectedBatchPathList.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename1");
		expectedBatchPathList.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename2");
		expectedBatchPathList.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename3");
		expectedBatchPathList.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename4");

		List<String> actualList = restoreService.findNextBatch(unzippedDirectories, uniqueName);
		assertThat(actualList, equalTo(expectedBatchPathList));
	}

	@Test
	public void findSnapshotDirectory() {
		List<String> allBackupFoldersList = generateAllBackupFoldersList();
		String expectedUniqueName = "sb1902250953";
		String actualUniqueName = restoreService.getSnapshotDirectories(allBackupFoldersList);
		assertThat(actualUniqueName, equalTo(expectedUniqueName));
	}

	@Test
	public void findBackupNameFromBackupPath() {
		String backupPath = "Discname:\\Users\\user.name\\backups\\clustername\\backup0000\\sb1902250953_relation_nodename1";
		String expectedUniqueName = "backup0000";
		String actualUniqueName = restoreService.findBackupNameFromBackupPath(backupPath);
		assertThat(actualUniqueName, equalTo(expectedUniqueName));
	}

	@Test
	public void distributeBackupPathsToNodes_backupPathsSizeGreaterThanNodeSize_success() {
		List<String> currentBatch = restoreRequest.getAllBackupFolders().stream()
				.filter(path -> path.contains("ib1902250953"))
				.collect(Collectors.toList());

		List<Integer> randomlySelectedNodeList = new ArrayList<>();
		randomlySelectedNodeList.add(5);
		randomlySelectedNodeList.add(1);
		randomlySelectedNodeList.add(4);

		Map<Integer, List<String>> exptectedBackupPathsToNodeList = generateDistributedBackupsPathtoNodesMap_backupSizeGreaterThanNodeCount();
		Map<Integer, List<String>> actualBackupPathsToNodeList = restoreService.distributeBackupPathsToNodes(currentBatch, randomlySelectedNodeList);
		assertThat(actualBackupPathsToNodeList, equalTo(exptectedBackupPathsToNodeList));

	}

	@Test
	public void distributeBackupPathsToNodes_backupPathsSizeSmallerThanNodeSize_success() {
		List<String> currentBatch = restoreRequest.getAllBackupFolders().stream()
				.filter(path -> path.contains("ib1902251053"))
				.collect(Collectors.toList());

		List<Integer> randomlySelectedNodeList = new ArrayList<>();
		randomlySelectedNodeList.add(5);
		randomlySelectedNodeList.add(1);
		randomlySelectedNodeList.add(4);

		Map<Integer, List<String>> exptectedBackupPathsToNodeList = generateDistributedBackupsPathtoNodesMap_backupSizeSmallerThanNodeCount();
		Map<Integer, List<String>> actualBackupPathsToNodeList = restoreService.distributeBackupPathsToNodes(currentBatch, randomlySelectedNodeList);
		assertThat(actualBackupPathsToNodeList, equalTo(exptectedBackupPathsToNodeList));

	}


	private Map<Integer, List<String>> generateDistributedBackupsPathtoNodesMap_backupSizeGreaterThanNodeCount() {
		Map<Integer, List<String>> nodeNumberRestorePathsMap = new HashMap<>();
		List<String> nodeBackups = new ArrayList<>();
		nodeBackups.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename1");
		nodeBackups.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename4");
		nodeNumberRestorePathsMap.put(5, nodeBackups);

		nodeBackups = new ArrayList<>();
		nodeBackups.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename2");
		nodeNumberRestorePathsMap.put(1, nodeBackups);

		nodeBackups = new ArrayList<>();
		nodeBackups.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename3");
		nodeNumberRestorePathsMap.put(4, nodeBackups);

		return nodeNumberRestorePathsMap;
	}

	private Map<Integer, List<String>> generateDistributedBackupsPathtoNodesMap_backupSizeSmallerThanNodeCount() {
		Map<Integer, List<String>> nodeNumberRestorePathsMap = new HashMap<>();
		List<String> nodeBackups = new ArrayList<>();
		nodeBackups.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902251053_relation_nodename1");
		nodeNumberRestorePathsMap.put(5, nodeBackups);

		nodeBackups = new ArrayList<>();
		nodeBackups.add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902251053_relation_nodename2");
		nodeNumberRestorePathsMap.put(1, nodeBackups);

		nodeBackups = new ArrayList<>();
		nodeNumberRestorePathsMap.put(4, nodeBackups);

		return nodeNumberRestorePathsMap;
	}

	private ClusterEntity generateClusterEntity() {

		ClusterEntity clusterEntity = new ClusterEntity();
		clusterEntity.setClusterName("cluster1");
		List<NodeEntity> nodeEntityList = new ArrayList<>();
		NodeEntity nodeEntity1 = new NodeEntity();
		nodeEntity1.setNodeName("node1");
		nodeEntity1.setClusterName("cluster1");
		nodeEntityList.add(nodeEntity1);

		NodeEntity nodeEntity2 = new NodeEntity();
		nodeEntity2.setNodeName("node2");
		nodeEntity2.setClusterName("cluster1");
		nodeEntityList.add(nodeEntity2);

		NodeEntity nodeEntity3 = new NodeEntity();
		nodeEntity3.setNodeName("node3");
		nodeEntity3.setClusterName("cluster1");
		nodeEntityList.add(nodeEntity3);

		NodeEntity nodeEntity4 = new NodeEntity();
		nodeEntity4.setNodeName("node4");
		nodeEntity4.setClusterName("cluster1");
		nodeEntityList.add(nodeEntity4);

		NodeEntity nodeEntity5 = new NodeEntity();
		nodeEntity5.setNodeName("node5");
		nodeEntity5.setClusterName("cluster1");
		nodeEntityList.add(nodeEntity5);

		NodeEntity nodeEntity6 = new NodeEntity();
		nodeEntity6.setNodeName("node6");
		nodeEntity6.setClusterName("cluster1");
		nodeEntityList.add(nodeEntity6);

		clusterEntity.setNodes(nodeEntityList);

		return clusterEntity;
	}

	private List<String> generateAllBackupFoldersList() {

		return new ArrayList<String>() {
			{
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\sb1902250953_relation_nodename1");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\sb1902251053_relation_nodename2");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902251153_relation_nodename1");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename1");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename2");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename3");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902250953_relation_nodename4");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902251053_relation_nodename1");
				add("Discname:\\Users\\user.name\\backups\\clustername\\backupname\\ib1902251053_relation_nodename2");
			}
		};

	}

	private List<String> generateKeyspaces() {

		return new ArrayList<String>() {
			{
				add("keyspace1");
			}
		};

	}
}
