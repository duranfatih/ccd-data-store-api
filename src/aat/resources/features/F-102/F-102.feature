@F-102
Feature: Get jurisdictions available to the user

  Background: Load test data for the scenario
    Given an appropriate test context as detailed in the test data source

  @S-533
  Scenario: must return a list of jurisdictions for a valid user
    Given a user with [a detailed profile in CCD having create case access for a jurisdiction]
    And a case that has just been created as in [Case_Creation_using_Caseworker1_Role]
    When a request is prepared with appropriate values
    And the request [has CREATE as case access]
    And it is submitted to call the [Get jurisdictions available to the user] operation of [CCD Data Store]
    Then a positive response is received
    And the response [contains the list of jurisdictions a user has access to]
    And the response has all other details as expected

  @S-534
  Scenario: must return 400 for if access type is not in create, read or update
    Given a user with [a detailed profile in CCD having create case access for a jurisdiction]
    And a case that has just been created as in [Case_Creation_using_Caseworker1_Role]
    When a request is prepared with appropriate values
    And the request [has DELETE as case access parameter]
    And it is submitted to call the [Get jurisdictions available to the user] operation of [CCD Data Store]
    Then a negative response is received
    And the response [returns the error message : Access can only be 'create', 'read' or 'update]
    And the response has all other details as expected

  @S-535
  Scenario: must return appropriate negative response for a user not having a profile in CCD
    Given a user with [no profile in CCD]
    When a request is prepared with appropriate values
    And the request [has CREATE as case access]
    And it is submitted to call the [Get jurisdictions available to the user] operation of [CCD Data Store]
    Then a negative response is received
    And the response [has the 403 return code]
    And the response has all the details as expected

  @S-536
  Scenario: must return appropriate negative response when request does not provide valid authorization credentials
    Given a user with [a detailed profile in CCD]
    When a request is prepared with appropriate values
    And the request [uses the invalid authorization]
    And it is submitted to call the [Get jurisdictions available to the user] operation of [CCD Data Store]
    Then a negative response is received
    And the response [has the 403 return code]
    And the response has all the details as expected
