package uk.gov.hmcts.ccd.domain.service.accessprofile.filter;

import com.google.common.collect.Lists;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.enums.GrantType;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignments;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.BeginDateEndDateMatcher;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.CaseIdMatcher;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.CaseTypeMatcher;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.JurisdictionMatcher;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.LocationMatcher;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.MatcherType;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.RegionMatcher;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.RoleAttributeMatcher;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.matcher.SecurityClassificationMatcher;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.service.casedataaccesscontrol.RoleAssignmentsFilteringServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleAssignmentsFilteringServiceImplTest extends BaseFilter {

    private RoleAssignmentsFilteringServiceImpl classUnderTest;

    @BeforeEach
    void setUp() {
        List<RoleAttributeMatcher> roleAttributeMatchers = Lists.newArrayList(new BeginDateEndDateMatcher(),
            new CaseIdMatcher(),
            new CaseTypeMatcher(),
            new JurisdictionMatcher(),
            new LocationMatcher(),
            new RegionMatcher(),
            new SecurityClassificationMatcher());
        classUnderTest = new RoleAssignmentsFilteringServiceImpl(roleAttributeMatchers);
    }

    @Test
    void shouldFilterBasedOnDateCaseIDJurisdiction() {
        RoleAssignments roleAssignments = mockRoleAssignments();
        CaseDetails caseDetails = mockCaseDetails();
        List<RoleAssignment> filteredRoleAssignments = classUnderTest
            .filter(roleAssignments, caseDetails).getFilteredMatchingRoleAssignments();
        assertEquals(1, filteredRoleAssignments.size());
    }


    @Test
    void shouldFilterBasedOnSecurityClassificationWhenCaseClassificationIsLess() {
        RoleAssignments roleAssignments = mockRoleAssignmentsOnSecurityClassification();
        CaseDetails caseDetails = mockCaseDetails();
        List<RoleAssignment> filteredRoleAssignments = classUnderTest
            .filter(roleAssignments, caseDetails).getFilteredMatchingRoleAssignments();
        assertEquals(2, filteredRoleAssignments.size());
    }

    @Test
    void shouldFilterBasedOnSecurityClassificationWhenCaseClassificationIsMore() {
        RoleAssignments roleAssignments = mockRoleAssignmentsOnSecurityClassification();
        CaseDetails caseDetails = mockCaseDetails(SecurityClassification.PRIVATE);
        List<RoleAssignment> filteredRoleAssignments = classUnderTest
            .filter(roleAssignments, caseDetails).getFilteredMatchingRoleAssignments();
        assertEquals(2, filteredRoleAssignments.size());
    }

    @Test
    void shouldFilterBasedOnSecurityClassificationWhenCaseClassificationIsRestricted() {
        RoleAssignments roleAssignments = mockRoleAssignmentsOnSecurityClassification();
        CaseDetails caseDetails = mockCaseDetails(SecurityClassification.RESTRICTED);
        List<RoleAssignment> filteredRoleAssignments = classUnderTest
            .filter(roleAssignments, caseDetails).getFilteredMatchingRoleAssignments();
        assertEquals(1, filteredRoleAssignments.size());
    }

    @Test
    void shouldFilterBasedOnStartDateAndEndDate() {
        RoleAssignments roleAssignments = mockRoleAssignmentsDatesNotMatching();
        CaseDetails caseDetails = mockCaseDetails();
        List<RoleAssignment> filteredRoleAssignments = classUnderTest
            .filter(roleAssignments, caseDetails).getFilteredMatchingRoleAssignments();
        assertEquals(0, filteredRoleAssignments.size());
    }

    private RoleAssignments mockRoleAssignments() {
        RoleAssignments roleAssignments = mock(RoleAssignments.class);

        RoleAssignment roleAssignment = createRoleAssignment(CASE_ID_1, JURISDICTION_1);
        RoleAssignment roleAssignment2 = createRoleAssignment(CASE_ID_2, JURISDICTION_2);

        List<RoleAssignment> roleAssignmentList = Lists.newArrayList(roleAssignment, roleAssignment2);

        when(roleAssignments.getRoleAssignments()).thenReturn(roleAssignmentList);

        return roleAssignments;
    }

    private RoleAssignments mockRoleAssignmentsOnSecurityClassification() {
        RoleAssignments roleAssignments = mock(RoleAssignments.class);

        RoleAssignment roleAssignment = createRoleAssignment(CASE_ID_1, JURISDICTION_1,
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS), "PRIVATE");
        RoleAssignment roleAssignment2 = createRoleAssignment(CASE_ID_1, JURISDICTION_1,
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS), "RESTRICTED");

        List<RoleAssignment> roleAssignmentList = Lists.newArrayList(roleAssignment, roleAssignment2);

        when(roleAssignments.getRoleAssignments()).thenReturn(roleAssignmentList);

        return roleAssignments;
    }

    private RoleAssignments mockRoleAssignmentsWithExcludedGrantType() {
        RoleAssignments roleAssignments = mock(RoleAssignments.class);

        RoleAssignment roleAssignment = createRoleAssignment(CASE_ID_1, JURISDICTION_1,
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS), "PRIVATE");
        roleAssignment.setGrantType(GrantType.EXCLUDED.name());
        RoleAssignment roleAssignment2 = createRoleAssignment(CASE_ID_1, JURISDICTION_1,
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS), "RESTRICTED");
        roleAssignment2.setGrantType(GrantType.EXCLUDED.name());

        List<RoleAssignment> roleAssignmentList = Lists.newArrayList(roleAssignment, roleAssignment2);

        when(roleAssignments.getRoleAssignments()).thenReturn(roleAssignmentList);

        return roleAssignments;
    }

    private RoleAssignments mockRoleAssignmentsDatesNotMatching() {
        RoleAssignments roleAssignments = mock(RoleAssignments.class);

        RoleAssignment roleAssignment = createRoleAssignment(CASE_ID_1, JURISDICTION_1,
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS), "PUBLIC");
        RoleAssignment roleAssignment2 = createRoleAssignment(CASE_ID_2, JURISDICTION_2,
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS), "PUBLIC");

        List<RoleAssignment> roleAssignmentList = Lists.newArrayList(roleAssignment, roleAssignment2);

        when(roleAssignments.getRoleAssignments()).thenReturn(roleAssignmentList);

        return roleAssignments;
    }

    @Test
    void shouldNotInvokeExcludedMatcher() {
        RoleAssignments roleAssignments = mockRoleAssignmentsWithExcludedGrantType();
        CaseTypeDefinition caseTypeDefinition = mockCaseTypeDefinition();
        List<RoleAssignment> filteredRoleAssignments = classUnderTest
            .filter(roleAssignments, caseTypeDefinition, Arrays.asList(MatcherType.GRANTTYPE, MatcherType.CASETYPE))
            .getFilteredMatchingRoleAssignments();
        assertEquals(2, filteredRoleAssignments.size());
    }

    @Test
    void shouldInvokeMatchersWhenExcludeMatchingListIsEmpty() {
        RoleAssignments roleAssignments = mockRoleAssignmentsWithExcludedGrantType();
        CaseTypeDefinition caseTypeDefinition = mockCaseTypeDefinition();
        List<RoleAssignment> filteredRoleAssignments = classUnderTest
            .filter(roleAssignments, caseTypeDefinition,
                Lists.newArrayList()).getFilteredMatchingRoleAssignments();
        assertEquals(0, filteredRoleAssignments.size());
    }
}
