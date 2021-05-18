package uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher;

import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleMatchingResult;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;

public interface RoleAttributeMatcher {

    void matchAttribute(Pair<RoleAssignment, RoleMatchingResult> resultPair, CaseDetails caseDetails);

    default boolean isValuesMatching(Optional<String> roleAssignmentValue,
                                     String caseDataValue) {
        //NOSONAR This comes from the Jackson deserializing Optional
        if (roleAssignmentValue == null) {
            return true;
        }
        return !roleAssignmentValue.isPresent()
            || roleAssignmentValue.get().equals(caseDataValue);
    }
}
