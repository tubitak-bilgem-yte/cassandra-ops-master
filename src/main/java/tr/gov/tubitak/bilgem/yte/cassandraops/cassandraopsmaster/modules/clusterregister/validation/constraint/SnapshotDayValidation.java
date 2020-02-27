package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.validation.constraint;


import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.validation.validator.SnapshotDaysValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SnapshotDaysValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SnapshotDayValidation {
    String message() default "Days must be between 1 and 28, both inclusive.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
