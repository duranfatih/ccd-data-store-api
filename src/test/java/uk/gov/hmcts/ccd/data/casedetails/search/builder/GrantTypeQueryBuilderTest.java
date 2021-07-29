package uk.gov.hmcts.ccd.data.casedetails.search.builder;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.GrantType;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignmentAttributes;

abstract class GrantTypeQueryBuilderTest {

    protected static final RoleAssignment createRoleAssignment(GrantType grantType,
                                                               String roleType,
                                                               String classification,
                                                               String location,
                                                               String region,
                                                               List<String> autorisations) {
        return createRoleAssignment(grantType, roleType, classification, "", location, region, autorisations);
    }

    protected static final RoleAssignment createRoleAssignment(GrantType grantType,
                                                             String roleType,
                                                             String classification,
                                                             String jurisdiction,
                                                             String location,
                                                             String region,
                                                             List<String> autorisations) {
        return createRoleAssignment(grantType, roleType, classification,
            jurisdiction, location, region, autorisations, "");
    }

    protected static final RoleAssignment createRoleAssignment(GrantType grantType,
                                                               String roleType,
                                                               String classification,
                                                               String jurisdiction,
                                                               String location,
                                                               String region,
                                                               List<String> autorisations,
                                                               String caseId) {
        RoleAssignmentAttributes attributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.ofNullable(jurisdiction))
            .location(Optional.ofNullable(location))
            .region(Optional.ofNullable(region))
            .caseId(Optional.ofNullable(caseId))
            .build();
        return RoleAssignment.builder()
            .grantType(grantType.name())
            .authorisations(autorisations)
            .roleType(roleType)
            .classification(classification)
            .attributes(attributes)
            .build();
    }
}