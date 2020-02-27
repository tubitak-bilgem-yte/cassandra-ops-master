package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.validation.validator;

import tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.clusterregister.validation.constraint.SnapshotDayValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

public class SnapshotDaysValidator implements ConstraintValidator<SnapshotDayValidation, Set<Integer>> {
    @Override
    public void initialize(final SnapshotDayValidation constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Set<Integer> snapshotDays, final ConstraintValidatorContext constraintValidatorContext) {
        for (Integer day : snapshotDays) {
            if (day < 1 || day > 28) {
                return false;
            }
        }
        return true;
    }
}
