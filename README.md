# Consultations API

This API is used to manage online consultations for treatments of various conditions.

Users are able to fetch a Consultation, which will include the Questions to be answered.

Users are also able to submit a Consultation Response, which will include the Answers to the Questions.

## Requirements

- Java 17
- Maven

## Installation

1. Clone the repository
2. Run `mvn clean compile` to compile the project
3. Run `mvn spring-boot:run` to start the application

## API Documentation

This service was built using an API-first approach.

The API specification can be found in `/src/main/resources/api/consultations-api.yaml`

Models and API Controller interfaces were generated using OpenAPI Generator.

In future I would expose the API documentation via an endpoint in the service, allowing users to view a Swagger page.

## Example requests

First ensure the service is running using `mvn spring-boot:run`

The service will automatically populate some data for you to test against.

The following are all curl commands based on request bodies stored in `.json` files in the `/requests` directory.

### Get Consultation by ID

#### Successful Fetch

```shell
curl localhost:8080/consultations/1
```

#### Consultation Not Found

```shell
curl localhost:8080/consultations/100
```

### Submit Consultation Response

#### Likely to Prescribe

```shell
curl localhost:8080/consultations/1/responses -X POST -d @requests/submit_consultation_response/likely_to_prescribe.json -H "Content-Type: application/json"
```

#### Unlikely to Prescribe

```shell
curl localhost:8080/consultations/1/responses -X POST -d @requests/submit_consultation_response/unlikely_to_prescribe.json -H "Content-Type: application/json"
```

#### Consultation Not Found

```shell
curl localhost:8080/consultations/100/responses -X POST -d @requests/submit_consultation_response/likely_to_prescribe.json -H "Content-Type: application/json"
```

#### Answers Missing

```shell
curl localhost:8080/consultations/1/responses -X POST -d @requests/submit_consultation_response/answers_missing.json -H "Content-Type: application/json"
```

#### Wrong Answer Type

```shell
curl localhost:8080/consultations/1/responses -X POST -d @requests/submit_consultation_response/wrong_answer_type.json -H "Content-Type: application/json"
```

## Design Decisions

Spring Boot was chosen due to ease of use, and was the quickest way to get a REST API up and running.

Spring Initializr was used to generate the project, via https://start.spring.io/

### Models

#### Consultation

Represents a unique consultation, containing a list of questions

#### Question

Represents a question that is part of a consultation.

Defines an answer type and answer validator.

#### Answer

Represents an answer to a question in a consultation.

Is a polymorphic type, with different types of answers supported. Currently only Boolean and Integer answers are supported.

#### Answer Validator

Defines a validation rule for an answer type. There can be many different validators for each answer type.

For example, there are 2 BooleanValidators:

- MustBeTrueValidator
- MustBeFalseValidator

Validators are currently defined as code, but instances can be stored in the database with any metadata required for perform the validation.

For example, the MustBeLessThanValidator - an extension of the IntegerValidator - has a 'threshold' field that defines the maximum value the answer
can be. 

A user could create multiple instances of this validator with different thresholds, and store those in the database to be used for different questions.

## Future Improvements

### Technical

- Improve test coverage - especially Mappers which I skipped due to time constraints - **there is no good reason to not unit test them**
- Introduce service layer
- Introduce a database for permanent storage
- Add validation to the requests
- Add API documentation endpoint

### Features

- Admin endpoint to create and manage consultations
- Admin endpoint to view consultation responses
- Support more answer types, like Date, Ranged Integer, etc
- Support more validation rules for answers
- Introduce role-based access control

## Technologies Used

- Java 17
- Spring Boot
- Lombok
- MapStruct
- OpenAPI Generator
- AssertJ