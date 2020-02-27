package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.copy.api.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
public class CopyArgs {
    private String sourcePath;
    private String destinationPath;
    private String copyRelation;
    private boolean deleteSourceAfterCopy;
}
