package uk.gov.hmcts.ccd.domain.service.casedataaccesscontrol;

import org.apache.commons.lang3.tuple.Pair;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.ApplicationParams;
import uk.gov.hmcts.ccd.data.SecurityUtils;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.AccessProfile;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignmentFilteringResult;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignments;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleMatchingResult;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.RoleToAccessProfileDefinition;
import uk.gov.hmcts.ccd.domain.service.AccessControl;
import uk.gov.hmcts.ccd.domain.service.common.CaseService;
import uk.gov.hmcts.ccd.domain.service.common.CaseTypeService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "enable-attribute-based-access-control", havingValue = "true")
public class DefaultCaseDataAccessControl implements CaseDataAccessControl, AccessControl {

    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;
    private final CaseService caseService;
    private final CaseTypeService caseTypeService;
    private final RoleAssignmentsFilteringService roleAssignmentsFilteringService;
    private final AccessProfileService accessProfileService;
    private final PseudoRoleAssignmentsGenerator pseudoRoleAssignmentsGenerator;
    private final ApplicationParams applicationParams;
    private final PseudoRoleToAccessProfileGenerator pseudoRoleToAccessProfileGenerator;

    @Autowired
    public DefaultCaseDataAccessControl(RoleAssignmentService roleAssignmentService,
                                        SecurityUtils securityUtils,
                                        CaseService caseService,
                                        CaseTypeService caseTypeService,
                                        RoleAssignmentsFilteringService roleAssignmentsFilteringService,
                                        PseudoRoleAssignmentsGenerator pseudoRoleAssignmentsGenerator,
                                        ApplicationParams applicationParams,
                                        AccessProfileService accessProfileService,
                                        PseudoRoleToAccessProfileGenerator pseudoRoleToAccessProfileGenerator) {
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.caseService = caseService;
        this.caseTypeService = caseTypeService;
        this.roleAssignmentsFilteringService = roleAssignmentsFilteringService;
        this.pseudoRoleAssignmentsGenerator = pseudoRoleAssignmentsGenerator;
        this.applicationParams = applicationParams;
        this.accessProfileService = accessProfileService;
        this.pseudoRoleToAccessProfileGenerator = pseudoRoleToAccessProfileGenerator;
    }

    // Returns Optional<CaseDetails>. If this is not enough think of wrapping it in a AccessControlResponse
    // that contains the additional information about the operation result
    @Override
    public List<AccessProfile> applyAccessControl(RoleAssignmentFilteringResult filteringResults, CaseTypeDefinition caseTypeDefinition ) {
        RoleAssignments roleAssignments = roleAssignmentService.getRoleAssignments(securityUtils.getUserId());

//        RoleAssignmentFilteringResult filteringResults = roleAssignmentsFilteringService
//            .filter(roleAssignments, caseDetails);

        if (applicationParams.getEnablePseudoRoleAssignmentsGeneration()) {
            List<RoleAssignment> pseudoRoleAssignments = pseudoRoleAssignmentsGenerator
                .createPseudoRoleAssignments(filteringResults);
            filteringResults = augment(filteringResults, pseudoRoleAssignments);
        }

//        CaseTypeDefinition caseTypeDefinition = caseTypeService.getCaseType(caseDetails.getCaseTypeId());

        if (filteringResults.hasGrantTypeExcludedRole()) {
            filteringResults = filteringResults.retainBasicAndSpecificGrantTypeRolesOnly();
        }

        List<AccessProfile> accessProfiles = generateAccessProfiles(filteringResults, caseTypeDefinition);

        return accessProfiles;
    }

    private List<AccessProfile> generateAccessProfiles(RoleAssignmentFilteringResult filteringResults,
                                                       CaseTypeDefinition caseTypeDefinition) {
        if (applicationParams.getEnablePseudoAccessProfilesGeneration()) {
            List<RoleToAccessProfileDefinition> pseudoAccessProfilesMappings =
                pseudoRoleToAccessProfileGenerator.generate(caseTypeDefinition);

            pseudoAccessProfilesMappings.addAll(caseTypeDefinition.getRoleToAccessProfiles());

            return accessProfileService
                .generateAccessProfiles(filteringResults, pseudoAccessProfilesMappings);
        } else {
            return accessProfileService
                .generateAccessProfiles(filteringResults, caseTypeDefinition.getRoleToAccessProfiles());
        }
    }

    private RoleAssignmentFilteringResult augment(RoleAssignmentFilteringResult filteringResults,
                                                  List<RoleAssignment> pseudoRoleAssignments) {
        List<Pair<RoleAssignment, RoleMatchingResult>> augmented = pseudoRoleAssignments
            .stream()
            .map(roleAssignment -> Pair.of(roleAssignment, new RoleMatchingResult()))
            .collect(Collectors.toList());

        augmented.addAll(filteringResults.getRoleMatchingResults());
        filteringResults = new RoleAssignmentFilteringResult(augmented);
        return filteringResults;
    }

    @Override
    public void grantAccess(String caseId, String idamUserId) {

    }
}
