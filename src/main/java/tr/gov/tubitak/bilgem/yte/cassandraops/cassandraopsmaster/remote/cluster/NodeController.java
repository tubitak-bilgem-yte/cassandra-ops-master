package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.NodeEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.NodeService;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto.NodeDTO;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/{cluster}")
public class NodeController {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/addNode")
    public String addNode(@RequestBody @Valid final NodeDTO nodeDTO, @PathVariable final String cluster) {
        NodeEntity nodeEntity = mapNodeDTOToEntity(nodeDTO, cluster);
        return nodeService.addNode(cluster, nodeEntity);
    }

    @PutMapping("/{node}/updateNode")
    public String updateNode(@PathVariable final String cluster, @PathVariable final String node, @RequestBody @Valid final NodeDTO nodeDTO) {
        NodeEntity nodeEntity = mapNodeDTOToEntity(nodeDTO, cluster);
        return nodeService.updateNode(cluster, node, nodeEntity);
    }

    @DeleteMapping("/{node}/deleteNode")
    public String deleteNode(@PathVariable final String node, @PathVariable final String cluster) {
        return nodeService.deleteNode(cluster, node);
    }

    private NodeEntity mapNodeDTOToEntity(@RequestBody @Valid final NodeDTO nodeDTO, @PathVariable final String cluster) {
        NodeEntity nodeEntity = modelMapper.map(nodeDTO, NodeEntity.class);
        nodeEntity.setClusterName(cluster);
        return nodeEntity;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public List<String> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
    }
}
