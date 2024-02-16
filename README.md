# Introduction

This repo contains a simple REST-assured project for running some notification callback tests against an internal ID&V
server that I worked on in the past.

The purpose is to make various HTTPS POSTs to a secured resource on a server in the test lab to check the HTTP status
codes returned from the server.

The test class runs:

- tests with valid notification payloads
- tests with invalid notification payloads
- tests with invalid, missing, null parameters
- tests with valid and invalid authentication tokens
- tests with missing HTTP payloads
- verify the HTTP Status codes

### Requirements

This example was written using the following:

- Java 8
- Maven
- Git
- REST-assured [here](https://rest-assured.io)
- AssertJ [here](https://assertj.github.io/doc/)

### Usage

To run the test class against the authentication endpoint, open a terminal window and enter the commands:

```
git clone https://github.com/dsmiles/notification-tests.git
cd notification-tests
mvn test
```

### Configuration:

In order to run the tests you must define the following environment variables either from the command line or your IDEs
run configuration:

| Environment Variable | Description                                       |
|----------------------|---------------------------------------------------|
| AUTHENTICATION_TOKEN | Your authentication token from the application UI |
| VERIFICATION_UUID    | Your verification UUID from the application UI    |
| APPLICANT_UUID       | Your applicant UUID from the application database |

To obtain the values you must log into your account and access your profile settings on the application UI. The 
applicant UUID must be retrieved from the database.

I could have defined these variables in a property file, but chose to use environment variables since there were only
three of them.
