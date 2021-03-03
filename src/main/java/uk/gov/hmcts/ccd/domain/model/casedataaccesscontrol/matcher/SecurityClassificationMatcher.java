package uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleMatchingResult;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;

import static uk.gov.hmcts.ccd.domain.service.common.SecurityClassificationUtils.caseHasClassificationEqualOrLowerThan;
import static uk.gov.hmcts.ccd.domain.service.common.SecurityClassificationUtils.getSecurityClassification;

@Slf4j
@Component
public class SecurityClassificationMatcher implements RoleAttributeMatcher {

    @Override
    public void matchAttribute(Pair<RoleAssignment, RoleMatchingResult> resultPai, CaseDetails caseDetails) {
        RoleAssignment roleAssignment = resultPai.getLeft();
        SecurityClassification caseDetailsSecurityClassification = caseDetails.getSecurityClassification();
        log.debug("Match role assignment security classification {} with case details security classification "
                + " {} for role assignment {}",
            roleAssignment.getClassification(),
            caseDetailsSecurityClassification,
            roleAssignment.getId());
        Optional<SecurityClassification> securityClassification = getSecurityClassification(roleAssignment
            .getClassification());
        if (securityClassification.isPresent()) {
            boolean value = caseHasClassificationEqualOrLowerThan(getSecurityClassification(roleAssignment
                .getClassification())
                .get())
                .test(caseDetails);
            resultPai.getRight().setClassificationMatched(value);
        }

        log.debug("Role assignment security classification {} and case details security classification "
                + " {} match {}",
            roleAssignment.getClassification(),
            caseDetailsSecurityClassification,
            resultPai.getRight().isClassificationMatched());
    }

}
