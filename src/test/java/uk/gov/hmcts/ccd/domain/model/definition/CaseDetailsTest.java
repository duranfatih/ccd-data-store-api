package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.CASE_REFERENCE;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.CASE_TYPE;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.CREATED_DATE;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.JURISDICTION;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.LAST_MODIFIED_DATE;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.LAST_STATE_MODIFIED_DATE;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.ccd.data.casedetails.search.MetaData.CaseField.STATE;


class CaseDetailsTest {

    private static final JsonNodeFactory JSON_NODE_FACTORY = new JsonNodeFactory(false);
    private static final String CASE_DETAIL_FIELD = "dataTestField1";

    private uk.gov.hmcts.ccd.domain.model.definition.CaseDetails caseDetails;

    @BeforeEach
    public void setup() {
        caseDetails = new uk.gov.hmcts.ccd.domain.model.definition.CaseDetails();
        Map<String, JsonNode> dataMap = buildData(CASE_DETAIL_FIELD);
        caseDetails.setData(dataMap);
    }

    @Test
    void testExistsInDataIsAlwaysTrueForLabels() {
        uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField tabField =
                createCaseTypeTabField("someId", "Label");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void testExistsInDataIsAlwaysTrueForCasePaymentHistoryViewer() {
        uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField tabField =
                createCaseTypeTabField("someId", "CasePaymentHistoryViewer");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void testExistsInDataIsAlwaysTrueForWaysToPay() {
        uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField tabField =
                createCaseTypeTabField("someId", "WaysToPay");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void testExistsInDataIsAlwaysTrueForFlagLauncher() {
        uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField tabField =
            createCaseTypeTabField("someId", "FlagLauncher");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void testExistsInDataIsFalseIfTabFieldDoesNotBelongToCase() {
        uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField tabField =
                createCaseTypeTabField("someId2", "YesOrNo");

        assertThat(caseDetails.existsInData(tabField), equalTo(false));
    }

    @Test
    void testExistsInDataIsTrueIfTabFieldBelongsToCase() {
        uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField tabField =
                createCaseTypeTabField(CASE_DETAIL_FIELD, "YesOrNo");

        assertThat(caseDetails.existsInData(tabField), equalTo(true));
    }

    @Test
    void shouldReturnCaseDataAndMetadataFieldMap() {
        LocalDateTime now = LocalDateTime.now();
        caseDetails.setJurisdiction("jurisdiction");
        caseDetails.setCaseTypeId("caseType");
        caseDetails.setState("state");
        caseDetails.setReference(1234567L);
        caseDetails.setCreatedDate(now);
        caseDetails.setLastModified(now);
        caseDetails.setSecurityClassification(SecurityClassification.PUBLIC);

        Map<String, Object> allData = caseDetails.getCaseDataAndMetadata();

        assertThat(((JsonNode) allData.get(CASE_DETAIL_FIELD)).asText(), equalTo(CASE_DETAIL_FIELD));
        assertThat(allData.get(JURISDICTION.getReference()), equalTo(caseDetails.getJurisdiction()));
        assertThat(allData.get(CASE_TYPE.getReference()), equalTo(caseDetails.getCaseTypeId()));
        assertThat(allData.get(STATE.getReference()), equalTo(caseDetails.getState()));
        assertThat(allData.get(CASE_REFERENCE.getReference()), equalTo(caseDetails.getReference()));
        assertThat(allData.get(CREATED_DATE.getReference()), equalTo(caseDetails.getCreatedDate()));
        assertThat(allData.get(LAST_MODIFIED_DATE.getReference()), equalTo(caseDetails.getLastModified()));
        assertThat(allData.get(LAST_STATE_MODIFIED_DATE.getReference()),
                equalTo(caseDetails.getLastStateModifiedDate()));
        assertThat(allData.get(SECURITY_CLASSIFICATION.getReference()),
                equalTo(caseDetails.getSecurityClassification()));
    }

    private Map<String, JsonNode> buildData(String... dataFieldIds) {
        Map<String, JsonNode> dataMap = Maps.newHashMap();
        Lists.newArrayList(dataFieldIds)
            .forEach(dataFieldId -> dataMap.put(dataFieldId, JSON_NODE_FACTORY.textNode(dataFieldId)));
        return dataMap;
    }

    private uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField createCaseTypeTabField(String id, String type) {
        uk.gov.hmcts.ccd.domain.model.definition.CaseFieldDefinition caseField =
                new uk.gov.hmcts.ccd.domain.model.definition.CaseFieldDefinition();
        caseField.setId(id);
        uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition labelFieldType =
                new uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition();
        labelFieldType.setType(type);
        caseField.setFieldTypeDefinition(labelFieldType);
        uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField tabField =
                new uk.gov.hmcts.ccd.domain.model.definition.CaseTypeTabField();
        tabField.setCaseFieldDefinition(caseField);
        return tabField;
    }

}
