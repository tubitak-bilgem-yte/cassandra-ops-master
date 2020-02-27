package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ClusterEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ClusterService;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto.ClusterDTO;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ClusterController {

    @Autowired
    private ClusterService registerClusterService;
    @Autowired
    private ModelMapper modelMapper;


    @PostMapping("/registerCluster")
    public String registerCluster(@Valid @RequestBody final ClusterDTO registerClusterRequest) {
        ClusterEntity clusterEntity = mapRequestDTOToEntity(registerClusterRequest);
        return registerClusterService.registerCluster(clusterEntity);
    }

    @PutMapping("/updateBackupPathOnDB")
    public String updateClusterBackupPath(@Valid @RequestBody final ClusterDTO registerClusterRequest) {
        ClusterEntity clusterEntity = mapRequestDTOToEntity(registerClusterRequest);
        return registerClusterService.updateBackupPathOnDB(clusterEntity);
    }

    @GetMapping("/{cluster}/getCluster")
    @ResponseBody
    public ClusterDTO getClusterInfo(@PathVariable final String cluster) {
        ClusterEntity dbEntity = registerClusterService.getClusterInfo(cluster);
        return modelMapper.map(dbEntity, ClusterDTO.class);
    }

    @DeleteMapping("/{cluster}/deleteCluster")
    public String deleteCluster(@PathVariable final String cluster) {
        return registerClusterService.deleteCluster(cluster);
    }

    /**
     * @param registerClusterRequest A DTO object transformed from json taken from user
     * @return Mapped object
     * This method transforms the DTO object to our actual entity object. It uses model mapper to do so. Also,
     * sets the relavent onformation for schedule.
     * @see ModelMapper#map(Object, Class)
     */

    private ClusterEntity mapRequestDTOToEntity(final ClusterDTO registerClusterRequest) {
        ClusterEntity clusterEntity = modelMapper.map(registerClusterRequest, ClusterEntity.class);
        if (clusterEntity.getNodes() != null) {
            clusterEntity.getNodes().forEach(e -> e.setClusterName(registerClusterRequest.getClusterName()));
        }
        if (registerClusterRequest.getSchedule() != null) {
            clusterEntity.getSchedule().setClusterName(registerClusterRequest.getClusterName());
            clusterEntity.getSchedule().setIsActive(true);
        }
        clusterEntity.setKeyspaces(String.join(",", registerClusterRequest.getKeyspaces()));

        return clusterEntity;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public List<String> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
    }

}
