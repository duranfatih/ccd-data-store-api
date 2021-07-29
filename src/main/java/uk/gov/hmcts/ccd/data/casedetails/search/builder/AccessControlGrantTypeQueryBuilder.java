package uk.gov.hmcts.ccd.data.casedetails.search.builder;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.RoleAssignment;

@Component
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class AccessControlGrantTypeQueryBuilder {

    private static final String QUERY = "( %s )";
    private static final String FINAL_QUERY = " AND ( %s )";

    private final BasicGrantTypeQueryBuilder basicGrantTypeQueryBuilder;
    private final SpecificGrantTypeQueryBuilder specificGrantTypeQueryBuilder;
    private final StandardGrantTypeQueryBuilder standardGrantTypeQueryBuilder;
    private final ChallengedGrantTypeQueryBuilder challengedGrantTypeQueryBuilder;
    private final ExcludedGrantTypeQueryBuilder excludedGrantTypeQueryBuilder;

    @Autowired
    public AccessControlGrantTypeQueryBuilder(BasicGrantTypeQueryBuilder basicGrantTypeQueryBuilder,
                                              SpecificGrantTypeQueryBuilder specificGrantTypeQueryBuilder,
                                              StandardGrantTypeQueryBuilder standardGrantTypeQueryBuilder,
                                              ChallengedGrantTypeQueryBuilder challengedGrantTypeQueryBuilder,
                                              ExcludedGrantTypeQueryBuilder excludedGrantTypeQueryBuilder) {
        this.basicGrantTypeQueryBuilder = basicGrantTypeQueryBuilder;
        this.specificGrantTypeQueryBuilder = specificGrantTypeQueryBuilder;
        this.standardGrantTypeQueryBuilder = standardGrantTypeQueryBuilder;
        this.challengedGrantTypeQueryBuilder = challengedGrantTypeQueryBuilder;
        this.excludedGrantTypeQueryBuilder = excludedGrantTypeQueryBuilder;
    }

    public String createQuery(List<RoleAssignment> roleAssignments, Map<String, Object> params) {
        String basicQuery = basicGrantTypeQueryBuilder.createQuery(roleAssignments, params);
        String specificQuery = specificGrantTypeQueryBuilder.createQuery(roleAssignments, params);
        String standardQuery = standardGrantTypeQueryBuilder.createQuery(roleAssignments, params);
        String challengedQuery = challengedGrantTypeQueryBuilder.createQuery(roleAssignments, params);

        String tmpQuery = mergeQuery(standardQuery, challengedQuery, " OR ");
        String excludedQuery = excludedGrantTypeQueryBuilder.createQuery(roleAssignments, params);
        if (StringUtils.isNotBlank(excludedQuery)) {
            tmpQuery = tmpQuery
                + getOperator(tmpQuery, " AND NOT ")
                + excludedQuery;
            tmpQuery = String.format(QUERY, tmpQuery);
        }

        String nonOrgQuery = mergeQuery(basicQuery, specificQuery, " OR ");
        if (StringUtils.isNotBlank(nonOrgQuery)) {
            tmpQuery = nonOrgQuery
                + getOperator(tmpQuery, " OR ")
                + tmpQuery;
        }

        return StringUtils.isNotBlank(tmpQuery) ? String.format(FINAL_QUERY, tmpQuery) : tmpQuery;
    }

    private String mergeQuery(String queryOne,
                              String queryTwo,
                              String operator) {
        String tmpQuery = queryOne;

        if (StringUtils.isNotBlank(queryTwo)) {
            return String.format(QUERY, tmpQuery + getOperator(tmpQuery, operator) + queryTwo);
        }
        return StringUtils.isNotBlank(tmpQuery) ? String.format(QUERY, tmpQuery) : tmpQuery;
    }

    private String getOperator(String query, String operator) {
        if (StringUtils.isNotBlank(query)) {
            return operator;
        }
        return "";
    }
}