package uk.gov.hmcts.ccd.domain.service.startevent;

import java.util.function.Supplier;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.casedetails.CachedCaseDetailsRepository;
import uk.gov.hmcts.ccd.data.casedetails.CaseDetailsRepository;
import uk.gov.hmcts.ccd.data.definition.CachedCaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.definition.CaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.draft.CachedDraftGateway;
import uk.gov.hmcts.ccd.data.draft.DraftGateway;
import uk.gov.hmcts.ccd.domain.model.callbacks.StartEventTrigger;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseEventDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.draft.Draft;
import uk.gov.hmcts.ccd.domain.model.draft.DraftResponse;
import uk.gov.hmcts.ccd.domain.service.callbacks.EventTokenService;
import uk.gov.hmcts.ccd.domain.service.common.CaseService;
import uk.gov.hmcts.ccd.domain.service.common.EventTriggerService;
import uk.gov.hmcts.ccd.domain.service.common.UIDService;
import uk.gov.hmcts.ccd.domain.service.getcase.CaseNotFoundException;
import uk.gov.hmcts.ccd.domain.service.stdapi.CallbackInvoker;
import uk.gov.hmcts.ccd.endpoint.exceptions.BadRequestException;
import uk.gov.hmcts.ccd.endpoint.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.ccd.endpoint.exceptions.ValidationException;
import uk.gov.hmcts.ccd.infrastructure.user.UserAuthorisation;


@Service
@Qualifier("default")
public class DefaultStartEventOperation implements StartEventOperation {

    private final EventTokenService eventTokenService;
    private final CaseDefinitionRepository caseDefinitionRepository;
    private final DraftGateway draftGateway;
    private final CaseDetailsRepository caseDetailsRepository;
    private final EventTriggerService eventTriggerService;
    private final CaseService caseService;
    private final UserAuthorisation userAuthorisation;
    private final CallbackInvoker callbackInvoker;
    private final UIDService uidService;

    @Autowired
    public DefaultStartEventOperation(final EventTokenService eventTokenService,
                                      @Qualifier(CachedCaseDefinitionRepository.QUALIFIER) final CaseDefinitionRepository caseDefinitionRepository,
                                      @Qualifier(CachedCaseDetailsRepository.QUALIFIER) final CaseDetailsRepository caseDetailsRepository,
                                      @Qualifier(CachedDraftGateway.QUALIFIER) final DraftGateway draftGateway,
                                      final EventTriggerService eventTriggerService,
                                      final CaseService caseService,
                                      final UserAuthorisation userAuthorisation,
                                      final CallbackInvoker callbackInvoker,
                                      final UIDService uidService) {

        this.eventTokenService = eventTokenService;
        this.caseDefinitionRepository = caseDefinitionRepository;
        this.caseDetailsRepository = caseDetailsRepository;
        this.draftGateway = draftGateway;
        this.eventTriggerService = eventTriggerService;
        this.caseService = caseService;
        this.userAuthorisation = userAuthorisation;
        this.callbackInvoker = callbackInvoker;
        this.uidService = uidService;
    }

    @Override
    public StartEventTrigger triggerStartForCaseType(final String caseTypeId,
                                                     final String eventId,
                                                     final Boolean ignoreWarning) {

        String uid = userAuthorisation.getUserId();

        final CaseTypeDefinition caseTypeDefinition = getCaseType(caseTypeId);

        return buildStartEventTrigger(uid,
            caseTypeDefinition,
                                      eventId,
                                      ignoreWarning,
                                      () -> caseService.createNewCaseDetails(caseTypeId, caseTypeDefinition.getJurisdictionId(), Maps.newHashMap()));
    }

    @Override
    public StartEventTrigger triggerStartForCase(final String caseReference,
                                                 final String eventId,
                                                 final Boolean ignoreWarning) {

        final CaseDetails caseDetails = getCaseDetails(caseReference);

        final String uid = userAuthorisation.getUserId();

        final CaseTypeDefinition caseTypeDefinition = getCaseType(caseDetails.getCaseTypeId());

        final CaseEventDefinition eventTrigger = getEventTrigger(eventId, caseTypeDefinition);

        validateEventTrigger(() -> !eventTriggerService.isPreStateValid(caseDetails.getState(), eventTrigger));

        final String eventToken = eventTokenService.generateToken(uid, caseDetails, eventTrigger, caseTypeDefinition.getJurisdiction(), caseTypeDefinition);

        callbackInvoker.invokeAboutToStartCallback(eventTrigger, caseTypeDefinition, caseDetails, ignoreWarning);

        return buildStartEventTrigger(eventId, eventToken, caseDetails);

    }

    @Override
    public StartEventTrigger triggerStartForDraft(final String draftReference,
                                                  final Boolean ignoreWarning) {
        final DraftResponse draftResponse = draftGateway.get(Draft.stripId(draftReference));
        final CaseDetails caseDetails = draftGateway.getCaseDetails(Draft.stripId(draftReference));

        final String uid = userAuthorisation.getUserId();

        final CaseTypeDefinition caseTypeDefinition = getCaseType(caseDetails.getCaseTypeId());

        return buildStartEventTrigger(uid,
            caseTypeDefinition,
                                      draftResponse.getDocument().getEventId(),
                                      ignoreWarning,
                                      () -> caseDetails);
    }

    private StartEventTrigger buildStartEventTrigger(final String uid,
                                                     final CaseTypeDefinition caseTypeDefinition,
                                                     final String eventId,
                                                     final Boolean ignoreWarning,
                                                     final Supplier<CaseDetails> caseDetailsSupplier) {
        final CaseEventDefinition eventTrigger = getEventTrigger(eventId, caseTypeDefinition);

        final CaseDetails caseDetails = caseDetailsSupplier.get();

        validateEventTrigger(() -> !eventTriggerService.isPreStateEmpty(eventTrigger));

        // TODO: we may need to take care of drafts that are saved for existing case so token needs to include the relevant draft payload
        final String eventToken = eventTokenService.generateToken(uid, eventTrigger, caseTypeDefinition.getJurisdiction(), caseTypeDefinition);

        callbackInvoker.invokeAboutToStartCallback(eventTrigger, caseTypeDefinition, caseDetails, ignoreWarning);

        return buildStartEventTrigger(eventId, eventToken, caseDetails);
    }

    private StartEventTrigger buildStartEventTrigger(String eventId, String eventToken, CaseDetails caseDetails) {
        final StartEventTrigger startEventTrigger = new StartEventTrigger();
        startEventTrigger.setCaseDetails(caseDetails);
        startEventTrigger.setToken(eventToken);
        startEventTrigger.setEventId(eventId);
        return startEventTrigger;
    }

    private CaseDetails getCaseDetails(String caseReference) {
        if (!uidService.validateUID(caseReference)) {
            throw new BadRequestException("Case reference is not valid");
        }

        return caseDetailsRepository.findByReference(caseReference).orElseThrow(
            () -> new CaseNotFoundException(caseReference));
    }

    private CaseEventDefinition getEventTrigger(String eventId, CaseTypeDefinition caseTypeDefinition) {
        final CaseEventDefinition eventTrigger = eventTriggerService.findCaseEvent(caseTypeDefinition, eventId);
        if (eventTrigger == null) {
            throw new ResourceNotFoundException("Cannot find event " + eventId + " for case type " + caseTypeDefinition.getId());
        }
        return eventTrigger;
    }

    private CaseTypeDefinition getCaseType(String caseTypeId) {
        final CaseTypeDefinition caseTypeDefinition = caseDefinitionRepository.getCaseType(caseTypeId);
        if (caseTypeDefinition == null) {
            throw new ResourceNotFoundException("Cannot find case type definition for " + caseTypeId);
        }
        return caseTypeDefinition;
    }

    private void validateEventTrigger(Supplier<Boolean> validationOperation) {
        if (validationOperation.get()) {
            throw new ValidationException("The case status did not qualify for the event");
        }
    }

}
