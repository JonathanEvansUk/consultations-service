package com.evans.consultations.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.evans.consultations.model.AnswerType;
import com.evans.consultations.model.AnswerValidator.BooleanValidator.MustBeTrueValidator;
import com.evans.consultations.model.Consultation;
import com.evans.consultations.model.ErrorDto;
import com.evans.consultations.model.Question;
import com.evans.consultations.repository.ConsultationRepository;
import com.evans.consultations.model.AnswerTypeDto;
import com.evans.consultations.model.BooleanAnswerDto;
import com.evans.consultations.model.ConsultationDto;
import com.evans.consultations.model.ConsultationResponseDto;
import com.evans.consultations.model.ConsultationResponseDto.StatusEnum;
import com.evans.consultations.model.IntegerAnswerDto;
import com.evans.consultations.model.QuestionDto;
import com.evans.consultations.model.SurveyResponseDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ConsultationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConsultationRepository consultationRepository;

    @BeforeEach
    void clearDatabase() {
        consultationRepository.deleteAll();
    }

    @Nested
    class GetConsultation {

        @Test
        void shouldFetchQuestions() {
            Question ageCheck = Question.builder()
                .id(10L)
                .text("Are you over 18?")
                .answerValidator(new MustBeTrueValidator())
                .answerType(AnswerType.BOOLEAN)
                .build();

            Consultation consultation = Consultation.builder()
                .id(99L)
                .questions(List.of(ageCheck))
                .build();

            consultationRepository.save(consultation);

            ResponseEntity<ConsultationDto> consultationById =
                restTemplate.getForEntity("http://localhost:" + port + "/consultations/{id}", ConsultationDto.class, 99L);

            assertThat(consultationById).isNotNull();
            assertThat(consultationById.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

            ConsultationDto responseBody = consultationById.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getId()).isEqualTo(99L);

            List<QuestionDto> questions = responseBody.getQuestions();
            assertThat(questions).hasSize(1);

            QuestionDto questionDto = questions.get(0);
            assertThat(questionDto.getId()).isEqualTo(10L);
            assertThat(questionDto.getText()).isEqualTo("Are you over 18?");
            assertThat(questionDto.getAnswerType()).isEqualTo(AnswerTypeDto.BOOLEAN);
        }

        @Test
        void shouldReturn404WhenConsultationNotFound() {
            ResponseEntity<ErrorDto> consultationById =
                restTemplate.getForEntity("http://localhost:" + port + "/consultations/{id}", ErrorDto.class, 100);

            assertThat(consultationById).isNotNull();
            assertThat(consultationById.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());

            ErrorDto errorDetails = consultationById.getBody();
            assertThat(errorDetails).isNotNull();
            assertThat(errorDetails.getMessage()).isEqualTo("Consultation not found");
        }
    }

    @Nested
    class SubmitAnswersForConsultation {

        @Test
        void shouldReturn200WhenAllAnswersPassValidation() {
            Question ageCheck = Question.builder()
                .id(1L)
                .text("Are you over 18?")
                .answerValidator(new MustBeTrueValidator())
                .answerType(AnswerType.BOOLEAN)
                .build();

            Consultation consultation = Consultation.builder()
                .id(1L)
                .questions(List.of(ageCheck))
                .build();

            consultationRepository.save(consultation);

            SurveyResponseDto request = SurveyResponseDto.builder()
                .answers(List.of(BooleanAnswerDto.builder().questionId(1L).value(true).build()))
                .build();

            ResponseEntity<ConsultationResponseDto> responseEntity =
                restTemplate.postForEntity(
                    "http://localhost:" + port + "/consultations/{consultationId}/responses",
                    request,
                    ConsultationResponseDto.class,
                    1
                );

            assertThat(responseEntity).isNotNull();
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

            ConsultationResponseDto responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getStatus()).isEqualTo(StatusEnum.REFERRED);
        }

        @Test
        void shouldReturn200WhenSomeAnswersFailValidation() {
            Question ageCheck = Question.builder()
                .id(1L)
                .text("Are you over 18?")
                .answerValidator(new MustBeTrueValidator())
                .answerType(AnswerType.BOOLEAN)
                .build();

            Consultation consultation = Consultation.builder()
                .id(1L)
                .questions(List.of(ageCheck))
                .build();

            consultationRepository.save(consultation);

            SurveyResponseDto request = SurveyResponseDto.builder()
                .answers(List.of(BooleanAnswerDto.builder().questionId(1L).value(false).build()))
                .build();

            ResponseEntity<ConsultationResponseDto> responseEntity =
                restTemplate.postForEntity(
                    "http://localhost:" + port + "/consultations/{consultationId}/responses",
                    request,
                    ConsultationResponseDto.class,
                    1
                );

            assertThat(responseEntity).isNotNull();
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

            ConsultationResponseDto responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getStatus()).isEqualTo(StatusEnum.FAILED);
        }

        @Test
        void shouldReturn404WhenConsultationNotFound() {
            SurveyResponseDto request = SurveyResponseDto.builder()
                .answers(List.of(BooleanAnswerDto.builder().questionId(1L).value(false).build()))
                .build();

            ResponseEntity<ErrorDto> responseEntity =
                restTemplate.postForEntity(
                    "http://localhost:" + port + "/consultations/{consultationId}/responses",
                    request,
                    ErrorDto.class,
                    1
                );

            assertThat(responseEntity).isNotNull();
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());

            ErrorDto errorDetails = responseEntity.getBody();
            assertThat(errorDetails).isNotNull();
            assertThat(errorDetails.getMessage()).isEqualTo("Consultation not found");
        }

        @Test
        void shouldReturn400WhenAnswersAreMissing() {
            Question ageCheck = Question.builder()
                .id(1L)
                .text("Are you over 18?")
                .answerValidator(new MustBeTrueValidator())
                .answerType(AnswerType.BOOLEAN)
                .build();

            Consultation consultation = Consultation.builder()
                .id(1L)
                .questions(List.of(ageCheck))
                .build();

            consultationRepository.save(consultation);

            SurveyResponseDto request = SurveyResponseDto.builder()
                .answers(List.of(BooleanAnswerDto.builder().questionId(2L).value(false).build()))
                .build();

            ResponseEntity<ErrorDto> responseEntity =
                restTemplate.postForEntity(
                    "http://localhost:" + port + "/consultations/{consultationId}/responses",
                    request,
                    ErrorDto.class,
                    1
                );

            assertThat(responseEntity).isNotNull();
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

            ErrorDto errorDetails = responseEntity.getBody();
            assertThat(errorDetails).isNotNull();
            assertThat(errorDetails.getMessage()).isEqualTo("Missing answers for questions: [1]");
        }

        @Test
        void shouldReturn400WhenAnswerTypeDoesNotMatchQuestionType() {
            Question ageCheck = Question.builder()
                .id(1L)
                .text("Are you over 18?")
                .answerValidator(new MustBeTrueValidator())
                .answerType(AnswerType.BOOLEAN)
                .build();

            Consultation consultation = Consultation.builder()
                .id(1L)
                .questions(List.of(ageCheck))
                .build();

            consultationRepository.save(consultation);

            SurveyResponseDto request = SurveyResponseDto.builder()
                .answers(List.of(IntegerAnswerDto.builder().questionId(1L).value(10).build()))
                .build();

            ResponseEntity<ErrorDto> responseEntity =
                restTemplate.postForEntity(
                    "http://localhost:" + port + "/consultations/{consultationId}/responses",
                    request,
                    ErrorDto.class,
                    1
                );

            assertThat(responseEntity).isNotNull();
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

            ErrorDto errorDetails = responseEntity.getBody();
            assertThat(errorDetails).isNotNull();
            assertThat(errorDetails.getMessage()).isEqualTo("Wrong answer type for following question ids: [1]");
        }
    }

}