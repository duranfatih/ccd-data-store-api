package uk.gov.hmcts.ccd.domain.service.getcase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;

import java.util.Optional;
import uk.gov.hmcts.ccd.domain.service.common.SecurityClassificationServiceImpl;

@Service
@Qualifier("classified")
@Slf4j
public class ClassifiedGetCaseOperation implements GetCaseOperation {


    private final GetCaseOperation getCaseOperation;
    private final SecurityClassificationServiceImpl classificationService;

    public ClassifiedGetCaseOperation(@Qualifier("default") GetCaseOperation getCaseOperation,
                                      SecurityClassificationServiceImpl classificationService) {
        this.getCaseOperation = getCaseOperation;
        this.classificationService = classificationService;
    }

    @Override
    public Optional<CaseDetails> execute(String jurisdictionId, String caseTypeId, String caseReference) {
        log.info("Get case operation Classified Jurisdiction {}  CaseTypeId {}, CaseReference {}",
            jurisdictionId, caseTypeId, caseReference);
        return getCaseOperation.execute(jurisdictionId, caseTypeId, caseReference)
                               .flatMap(classificationService::applyClassification);
    }

    @Override
    public Optional<CaseDetails> execute(String caseReference) {
        log.info("Get case operation Classified CaseReference {}", caseReference);
        return getCaseOperation.execute(caseReference)
                               .flatMap(classificationService::applyClassification);
    }
}
