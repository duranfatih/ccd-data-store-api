package uk.gov.hmcts.ccd.domain.dto.globalsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class GlobalSearchResponse {
    ResultInfo resultInfo;
    List<Result> results;

    @Value
    @Builder
    @Jacksonized
    public static class ResultInfo {
        int casesReturned;
        int caseStartRecord;
        boolean moreResultsToGo;
    }

    @Value
    @Builder
    @Jacksonized
    public static class Result {
        String stateId;
        String processForAccess;
        String caseReference;
        String otherReference;
        @JsonProperty("CCDJurisdictionId")
        String ccdJurisdictionId;
        @JsonProperty("CCDJurisdictionName")
        String ccdJurisdictionName;
        @JsonProperty("HMCTSServiceId")
        String hmctsServiceId;
        @JsonProperty("HMCTSServiceShortDescription")
        String hmctsServiceShortDescription;
        @JsonProperty("CCDCaseTypeId")
        String ccdCaseTypeId;
        @JsonProperty("CCDCaseTypeName")
        String ccdCaseTypeName;
        String caseNameHmctsInternal;
        String baseLocationId;
        String baseLocationName;
        String caseManagementCategoryId;
        String caseManagementCategoryName;
        String regionId;
        String regionName;
    }
}
