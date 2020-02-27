package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleEntityRepository extends JpaRepository<ScheduleEntity, String> {

    ScheduleEntity findByClusterName(String clusterName);
}
