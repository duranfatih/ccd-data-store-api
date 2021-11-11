package uk.gov.hmcts.ccd.domain.service.casedeletion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.config.JacksonObjectMapperConfig;
import uk.gov.hmcts.ccd.domain.model.casedeletion.TTL;
import uk.gov.hmcts.ccd.domain.model.definition.CaseEventDefinition;
import uk.gov.hmcts.ccd.endpoint.exceptions.BadRequestException;
import uk.gov.hmcts.ccd.endpoint.exceptions.ValidationException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class TimeToLiveServiceTest {

    @InjectMocks
    private TimeToLiveService timeToLiveService;
    private CaseEventDefinition caseEventDefinition;
    private static final Integer TTL_INCREMENT = 10;
    private Map<String, JsonNode> caseData = new HashMap<>();

    @Spy
    private ObjectMapper objectMapper = new JacksonObjectMapperConfig().defaultObjectMapper();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        caseData.put("key", objectMapper.readTree("{\"Value\": \"value\"}"));
        caseEventDefinition = new CaseEventDefinition();
    }

    @Test
    void updateCaseDetailsWithNullCaseData() {
        assertNull(timeToLiveService.updateCaseDetailsWithTTL(null, caseEventDefinition));
    }

    @Test
    void updateCaseDetailsWithTTLNoTtlIncrementSet() {
        assertEquals(caseData, timeToLiveService.updateCaseDetailsWithTTL(caseData, caseEventDefinition));
    }

    @Test
    void updateCaseDetailsTTLCaseFieldPresentNoTTLIncrementSet() {
        TTL ttl = TTL.builder().systemTTL(LocalDate.now()).build();
        caseData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(ttl));
        assertEquals(caseData, timeToLiveService.updateCaseDetailsWithTTL(caseData, caseEventDefinition));
    }

    @Test
    void updateCaseDetailsTTLCaseFieldPresentTTLIncrementSet() {
        TTL ttl = TTL.builder().systemTTL(LocalDate.now()).build();
        caseData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(ttl));

        Map<String, JsonNode> expectedCaseData = addDaysToSystemTTL(caseData, TTL_INCREMENT);

        caseEventDefinition.setTtlIncrement(TTL_INCREMENT);

        assertEquals(expectedCaseData, timeToLiveService.updateCaseDetailsWithTTL(caseData, caseEventDefinition));
    }

    @Test
    void updateCaseDetailsThrowsValidationExceptionWhenJsonParsingFails() throws IOException {
        TTL ttl = TTL.builder().systemTTL(LocalDate.now()).build();
        String ttlNodeAsString = objectMapper.valueToTree(ttl).toString();

        caseData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(ttl));
        caseEventDefinition.setTtlIncrement(TTL_INCREMENT);

        doThrow(JsonProcessingException.class).when(objectMapper).readValue(ttlNodeAsString, TTL.class);


        Exception exception = assertThrows(ValidationException.class, () -> {
            timeToLiveService.updateCaseDetailsWithTTL(caseData, caseEventDefinition);
        });

        assertEquals(TimeToLiveService.FAILED_TO_READ_TTL_FROM_CASE_DATA, exception.getMessage());
    }

    @Test
    void verifyTTLContentNotChangedNullCaseData() {
        timeToLiveService.verifyTTLContentNotChanged(null, null);
    }

    @Test
    void verifyTTLContentNotChangedNoTTLInCaseData() {
        Map<String, JsonNode> expectedCaseData = new HashMap<>(caseData);

        timeToLiveService.verifyTTLContentNotChanged(expectedCaseData, caseData);
    }

    @Test
    void verifyTTLContentNotChangedTTLValuesUnchanged() {
        TTL ttl = TTL.builder().systemTTL(LocalDate.now()).build();
        caseData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(ttl));

        Map<String, JsonNode> expectedCaseData = new HashMap<>(caseData);
        expectedCaseData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(ttl));

        timeToLiveService.verifyTTLContentNotChanged(expectedCaseData, caseData);
    }

    @Test
    void verifyTTLContentNotChangedTTLValuesChanged() {
        TTL ttl = TTL.builder().systemTTL(LocalDate.now()).build();
        caseData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(ttl));

        Map<String, JsonNode> expectedCaseData = addDaysToSystemTTL(caseData, TTL_INCREMENT);

        Exception exception = assertThrows(BadRequestException.class, () -> {
            timeToLiveService.verifyTTLContentNotChanged(expectedCaseData, caseData);
        });

        assertEquals(TimeToLiveService.TIME_TO_LIVE_MODIFIED_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void verifyTTLContentNotChangedThrowsValidationExceptionWhenJsonParsingFails() throws IOException {
        TTL ttl = TTL.builder().systemTTL(LocalDate.now()).build();
        String ttlNodeAsString = objectMapper.valueToTree(ttl).toString();

        caseData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(ttl));
        caseEventDefinition.setTtlIncrement(TTL_INCREMENT);

        doThrow(JsonProcessingException.class).when(objectMapper).readValue(ttlNodeAsString, TTL.class);


        Exception exception = assertThrows(ValidationException.class, () -> {
            timeToLiveService.verifyTTLContentNotChanged(caseData, caseData);
        });

        assertEquals(TimeToLiveService.FAILED_TO_READ_TTL_FROM_CASE_DATA, exception.getMessage());
    }

    private Map<String, JsonNode> addDaysToSystemTTL(Map<String, JsonNode> data, Integer numOfDays) {
        Map<String, JsonNode> clonedData = new HashMap<>(data);
        TTL expectedTtl = TTL.builder()
            .systemTTL(LocalDate.now().plusDays(numOfDays))
            .build();
        clonedData.put(TTL.TTL_CASE_FIELD_ID, objectMapper.valueToTree(expectedTtl));
        return clonedData;
    }
}