@ash
Feature: Access Control Search Tests

  Background: Load test data for the scenario
    Given an appropriate test context as detailed in the test data source

    @S-202.1
    Scenario: User cannot get a case that has higher SC than the users Role Assignemnt
      Given a user with [restricted access to create a case J1-CT2-01]
      And a user with [PUBLIC SC ORGANISATION role assignment access]
      And a case that has just been created as in [F202_Create_Case_CT1]
      And a successful call [to give user Solicitor1 a PUBLIC CASE role assignment to view the previously created case J1-CT2-01] as in [S-202.1_Grant_Role_Assignment]
      When a request is prepared with appropriate values
      And the request [attempts to search for case J1-CT2-01]
      And it is submitted to call the [ES Search] operation of [CCD Data Store]
      Then a positive response is received
      Then the response [contains no cases]
      And the response has all other details as expected
