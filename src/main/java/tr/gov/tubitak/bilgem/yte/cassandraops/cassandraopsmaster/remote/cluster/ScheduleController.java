package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.persistance.ScheduleEntity;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.service.ScheduleService;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.cluster.dto.ScheduleDTO;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/{cluster}")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/addSchedule")
    public String addSchedule(@PathVariable final String cluster, @RequestBody @Valid final ScheduleDTO scheduleDTO) {
        ScheduleEntity scheduleEntity = mapScheduleDTOToEntity(scheduleDTO, cluster);
        return scheduleService.addSchedule(cluster, scheduleEntity);
    }

    @PutMapping("/updateSchedule")
    public String updateSchedule(@PathVariable final String cluster, @RequestBody @Valid final ScheduleDTO scheduleDTO) {
        ScheduleEntity scheduleEntity = mapScheduleDTOToEntity(scheduleDTO, cluster);
        return scheduleService.updateSchedule(cluster, scheduleEntity);
    }

    @PutMapping("/disableSchedule")
    public String disableSchedule(@PathVariable final String cluster) {
        return scheduleService.disableSchedule(cluster);
    }

    @PutMapping("/enableSchedule")
    public String enableSchedule(@PathVariable final String cluster) {
        return scheduleService.enableSchedule(cluster);
    }

    @DeleteMapping("/deleteSchedule")
    public String deleteSchedule(@PathVariable final String cluster) {
        return scheduleService.deleteSchedule(cluster);
    }

    private ScheduleEntity mapScheduleDTOToEntity(final ScheduleDTO scheduleDTO, final String clusterName) {
        ScheduleEntity scheduleEntity = modelMapper.map(scheduleDTO, ScheduleEntity.class);
        scheduleEntity.setClusterName(clusterName);
        return scheduleEntity;
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
