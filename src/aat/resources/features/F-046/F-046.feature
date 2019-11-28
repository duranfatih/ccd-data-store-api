@F-048
Feature: Revoke access to case

  Background: Load test data for the scenario
    Given an appropriate test context as detailed in the test data source

#  @S-222
#  Scenario: must return 204 if access is successfully revoked for a user on a case ID
#    Given a case has been created as in [Standard-Full-Case]
#    And a successful call [Get case] as in [theRequest.caseId]
#    And a user with [an active profile in CCD]
#    When a request is prepared with appropriate values
#    And it is submitted to call the [Revoke access to case] operation of [CCD Data Store]
#    Then a positive response is received
#    And the response [has a 204 no content code]
#    And the response has all other details as expected
#    And a call [Get case] will get the expected response as in [XXXX-XXXX-XXXX-XXXX]

  @S-223
  Scenario: must return 400 if case id is invalid
    Given a user with [a detailed profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an invalid case id]
    And it is submitted to call the [Revoke access to case] operation of [CCD Data Store]
    Then a negative response is received
    And the response [has a 400 bad request code]
    And the response has all other details as expected

  @S-224
  Scenario: must return 401 when request does not provide valid authentication credentials
    Given a user with [a detailed profile in CCD]
    When a request is prepared with appropriate values
    And the request [does not provide valid authentication credentials in CCD]
    And it is submitted to call the [Revoke access to case] operation of [CCD Data Store]
    Then a negative response is received
    And the response [has a 401 Unauthorized code]
    And the response has all other details as expected

  @S-225
  Scenario: must return 403 when request provides authentic credentials without authorized access to the operation
    Given a user with [a detailed profile in CCD]
    When a request is prepared with appropriate values
    And the request [does not provide valid authentication credentials in CCD]
    And it is submitted to call the [Revoke access to case] operation of [CCD Data Store]
    Then a negative response is received
    And the response [has a 403 Forbidden code]
    And the response has all other details as expected


