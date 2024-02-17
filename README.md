# Notification Callback Tests

This repository contains a simple REST-assured project for running notification callback tests against an internal ID&V server.

The purpose is to send various HTTPS POST requests to a secured resource on a server in the test lab and verify the HTTP status codes returned.

The test class covers the following scenarios:

- Tests with valid notification payloads
- Tests with invalid notification payloads
- Tests with invalid, missing, or null parameters
- Tests with valid and invalid authentication tokens
- Tests with missing HTTP payloads
- Verification of the HTTP status codes

## Requirements

This example was written using the following:

- Java 8
- Maven
- Git
- REST-assured [here](https://rest-assured.io)
- AssertJ [here](https://assertj.github.io/doc/)

## Usage

To run the test class against the authentication endpoint, follow these steps:

1. Clone the repository:
```
git clone https://github.com/dsmiles/notification-tests.git
cd notification-tests
mvn test
```
 
2. Run Maven test command:
```
mvn test
```

## Configuration:

To run the tests, you must define the following environment variables either from the command line or your IDE's run 
configuration:

| Environment Variable | Description                                       |
|----------------------|---------------------------------------------------|
| AUTHENTICATION_TOKEN | Your authentication token from the application UI |
| VERIFICATION_UUID    | Your verification UUID from the application UI    |
| APPLICANT_UUID       | Your applicant UUID from the application database |

To obtain the values, log into your account and access your profile settings on the application UI. The applicant UUID 
must be retrieved from the database.

Environment variables were chosen over a property file since there are only three of them.
