package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.remote.restore;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.RestoreResponseHandler;
import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.restore.service.RestoreService;

@RestController
@RequiredArgsConstructor
public class RestoreController {

    private final RestoreResponseHandler restoreResponseHandler;
    private final ModelMapper modelMapper;
    private final RestoreService restoreService;



    @PostMapping("/batchRestoreResult")
    public void restoreResultReceive(@RequestBody final BatchRestoreResponse batchRestoreResponse) {
        restoreResponseHandler.handle(batchRestoreResponse);
    }

    @PostMapping("/restore")
    public void restore(@RequestBody final RestoreRequest restoreRequest) {
        restoreService.restore(restoreRequest);
    }

    @PostMapping("/restoreFinish")
    public void restoreFinish(final String responseMessage) {
        System.out.println(responseMessage);
    }


}
