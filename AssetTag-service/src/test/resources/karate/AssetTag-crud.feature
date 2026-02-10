# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check AssetTag Rest Api Response

  Scenario: AssetTag CRUD Operations

    * def tagName = 'Test Tag CRUD ' + randomSeed
    * def tagNameUpdated = 'Test Tag CRUD Updated ' + randomSeed

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/assettags'
    # ---- Add entity fields here -----
    And request
    """ {
      "name": "#(tagName)",
      "description": "A test tag",
      "color": "#FF0000"
    } """
    # ---------------------------------
    When method POST
    Then status 200
    # ---- Matching required response json ----
    And match response.id == '#number'
    And match response.entityVersion == 1
    And match response.name == tagName
    And match response.description == 'A test tag'
    And match response.color == '#FF0000'
    * def entityId = response.id

    # --------------- UPDATE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/assettags'
    # ---- Add entity fields here -----
    And request
    """ {
          "id":"#(entityId)",
          "entityVersion":1,
          "name": "#(tagNameUpdated)",
          "description": "An updated test tag",
          "color": "#00FF00"
    }
    """
    # ---------------------------------
    When method PUT
    Then status 200
    # ---- Matching required response json ----
    And match response.id == entityId
    And match response.entityVersion == 2
    And match response.name == tagNameUpdated
    And match response.description == 'An updated test tag'
    And match response.color == '#00FF00'

  # --------------- FIND -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/assettags/'+entityId
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match response.id == entityId
    And match response.entityVersion == 2
    And match response.name == tagNameUpdated

  # --------------- FIND ALL -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/assettags'
    When method GET
    Then status 200
    And match response.results != '#[0]'

  # --------------- DELETE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/assettags/'+entityId
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
