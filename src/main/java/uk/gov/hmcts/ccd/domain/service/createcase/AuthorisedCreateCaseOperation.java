package uk.gov.hmcts.ccd.domain.service.createcase;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.config.JacksonUtils;
import uk.gov.hmcts.ccd.data.definition.CachedCaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.definition.CaseDefinitionRepository;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.std.CaseDataContent;
import uk.gov.hmcts.ccd.domain.model.std.Event;
import uk.gov.hmcts.ccd.domain.model.std.SupplementaryDataUpdateRequest;
import uk.gov.hmcts.ccd.domain.model.std.validator.SupplementaryDataUpdateRequestValidator;
import uk.gov.hmcts.ccd.domain.service.common.AccessControlService;
import uk.gov.hmcts.ccd.domain.service.common.CaseAccessService;
import uk.gov.hmcts.ccd.domain.service.supplementarydata.SupplementaryDataUpdateOperation;
import uk.gov.hmcts.ccd.endpoint.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.ccd.endpoint.exceptions.ValidationException;

import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.CAN_CREATE;
import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.CAN_READ;
import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.NO_CASE_TYPE_FOUND;
import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.NO_EVENT_FOUND;
import static uk.gov.hmcts.ccd.domain.service.common.AccessControlService.NO_FIELD_FOUND;

@Service
@Qualifier("authorised")
public class AuthorisedCreateCaseOperation implements CreateCaseOperation {

    private final CreateCaseOperation createCaseOperation;
    private final CaseDefinitionRepository caseDefinitionRepository;
    private final AccessControlService accessControlService;
    private final CaseAccessService caseAccessService;
    private SupplementaryDataUpdateOperation supplementaryDataUpdateOperation;
    private SupplementaryDataUpdateRequestValidator validator;

    public AuthorisedCreateCaseOperation(@Qualifier("classified") final CreateCaseOperation createCaseOperation,
                                         @Qualifier(CachedCaseDefinitionRepository.QUALIFIER)
                                         final CaseDefinitionRepository caseDefinitionRepository,
                                         final AccessControlService accessControlService,
                                         final CaseAccessService caseAccessService,
                                         @Qualifier("authorised")
                                             SupplementaryDataUpdateOperation supplementaryDataUpdateOperation,
                                         SupplementaryDataUpdateRequestValidator validator) {

        this.createCaseOperation = createCaseOperation;
        this.caseDefinitionRepository = caseDefinitionRepository;
        this.accessControlService = accessControlService;
        this.caseAccessService = caseAccessService;
        this.supplementaryDataUpdateOperation = supplementaryDataUpdateOperation;
        this.validator = validator;
    }

    @Override
    public CaseDetails createCaseDetails(String caseTypeId,
                                         CaseDataContent caseDataContent,
                                         Boolean ignoreWarning) {
        if (caseDataContent == null) {
            throw new ValidationException("No data provided");
        }

        final CaseTypeDefinition caseTypeDefinition = caseDefinitionRepository.getCaseType(caseTypeId);
        if (caseTypeDefinition == null) {
            throw new ValidationException("Cannot find case type definition for  " + caseTypeId);
        }

        Set<String> userRoles = caseAccessService.getCaseCreationRoles();

        Event event = caseDataContent.getEvent();
        Map<String, JsonNode> data = caseDataContent.getData();
        verifyCreateAccess(event, data, caseTypeDefinition, userRoles);

        final CaseDetails caseDetails = createCaseOperation.createCaseDetails(caseTypeId,
            caseDataContent,
            ignoreWarning);
        createSupplementaryData(caseDataContent, caseDetails);
        return verifyReadAccess(caseTypeDefinition, userRoles, caseDetails);
    }

    private void createSupplementaryData(CaseDataContent caseDataContent, CaseDetails caseDetails) {
        SupplementaryDataUpdateRequest supplementaryDataUpdateRequest = caseDataContent
            .getSupplementaryDataUpdateRequest();
        if (supplementaryDataUpdateRequest != null) {
            validator.validate(supplementaryDataUpdateRequest);
        }
        supplementaryDataUpdateOperation.updateSupplementaryData(caseDetails.getReferenceAsString(),
            caseDataContent.getSupplementaryDataUpdateRequest());
    }

    private CaseDetails verifyReadAccess(CaseTypeDefinition caseTypeDefinition, Set<String> userRoles,
                                         CaseDetails caseDetails) {

        if (caseDetails != null) {
            if (!accessControlService.canAccessCaseTypeWithCriteria(
                caseTypeDefinition,
                userRoles,
                CAN_READ)) {
                return null;
            }

            caseDetails.setData(JacksonUtils.convertValue(
                accessControlService.filterCaseFieldsByAccess(
                    JacksonUtils.convertValueJsonNode(caseDetails.getData()),
                    caseTypeDefinition.getCaseFieldDefinitions(),
                    userRoles,
                    CAN_READ, false)));
            caseDetails.setDataClassification(JacksonUtils.convertValue(
                accessControlService.filterCaseFieldsByAccess(
                    JacksonUtils.convertValueJsonNode(caseDetails.getDataClassification()),
                    caseTypeDefinition.getCaseFieldDefinitions(),
                    userRoles,
                    CAN_READ,
                    true)
            ));
        }
        return caseDetails;
    }

    private void verifyCreateAccess(Event event, Map<String, JsonNode> data, CaseTypeDefinition caseTypeDefinition,
                                    Set<String> userRoles) {
        if (!accessControlService.canAccessCaseTypeWithCriteria(
            caseTypeDefinition,
            userRoles,
            CAN_CREATE)) {
            throw new ResourceNotFoundException(NO_CASE_TYPE_FOUND);
        }

        if (event == null || !accessControlService.canAccessCaseEventWithCriteria(
            event.getEventId(),
            caseTypeDefinition.getEvents(),
            userRoles,
            CAN_CREATE)) {
            throw new ResourceNotFoundException(NO_EVENT_FOUND);
        }

        if (!accessControlService.canAccessCaseFieldsWithCriteria(
            JacksonUtils.convertValueJsonNode(data),
            caseTypeDefinition.getCaseFieldDefinitions(),
            userRoles,
            CAN_CREATE)) {
            throw new ResourceNotFoundException(NO_FIELD_FOUND);
        }
    }
}
