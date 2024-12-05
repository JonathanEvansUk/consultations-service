package com.evans.consultations.controller;

import com.evans.consultations.exception.ConsultationsException;
import com.evans.consultations.api.ConsultationsApi;
import com.evans.consultations.model.Answer;
import com.evans.consultations.model.AnswerType;
import com.evans.consultations.model.AnswerValidator;
import com.evans.consultations.model.Consultation;
import com.evans.consultations.model.ConsultationDto;
import com.evans.consultations.model.ConsultationResponse;
import com.evans.consultations.model.ConsultationResponse.Status;
import com.evans.consultations.model.ConsultationResponseDto;
import com.evans.consultations.model.Question;
import com.evans.consultations.model.SurveyResponseDto;
import com.evans.consultations.repository.ConsultationRepository;
import com.evans.consultations.transformer.ConsultationMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConsultationController implements ConsultationsApi {

    private final ConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;

    @Override
    public ResponseEntity<ConsultationDto> getConsultationById(Long id) {
        log.info("Received request to get consultation by Id: {}", id);
        Consultation consultation = consultationRepository.getById(id);

        if (consultation == null) {
            throw new ConsultationsException(HttpStatus.NOT_FOUND, "Consultation not found");
        }

        ConsultationDto consultationDto = consultationMapper.mapToConsultation(consultation);

        return ResponseEntity.ok(consultationDto);
    }

    @Override
    public ResponseEntity<ConsultationResponseDto> submitResponse(Long id, SurveyResponseDto surveyResponse) {
        log.info("Received answers for consultation with Id: {}, {}", id, surveyResponse);

        Consultation consultation = consultationRepository.getById(id);
        if (consultation == null) {
            throw new ConsultationsException(HttpStatus.NOT_FOUND, "Consultation not found");
        }

        List<Answer<?>> answers = consultationMapper.mapToAnswers(surveyResponse.getAnswers());

        Map<Long, Answer<?>> answersByQuestionId = answers.stream()
            .collect(Collectors.toMap(Answer::questionId, Function.identity()));

        List<Long> missingQuestionIds = consultation.questions()
            .stream()
            .map(Question::id)
            .filter(questionId -> !answersByQuestionId.containsKey(questionId))
            .toList();

        if (!missingQuestionIds.isEmpty()) {
            log.error("Missing answers for questions: {}", missingQuestionIds);
            // could return some custom error code so the UI can know what to display
            throw new ConsultationsException(HttpStatus.BAD_REQUEST, "Missing answers for questions: " + missingQuestionIds);
        }

        record QuestionAndAnswer(Question question, Answer<?> answer) {}

        List<QuestionAndAnswer> questionAndAnswers = consultation.questions()
            .stream()
            .map(question -> new QuestionAndAnswer(question, answersByQuestionId.get(question.id())))
            .toList();

        // verify that all answers match the answerType that the question expects
        List<QuestionAndAnswer> questionAndAnswersWithNonMatchingType = questionAndAnswers.stream()
            .filter(questionAndAnswer -> questionAndAnswer.answer().type() != questionAndAnswer.question().answerType())
            .toList();

        if (!questionAndAnswersWithNonMatchingType.isEmpty()) {
            log.error("Answers with non-matching types: {}", questionAndAnswersWithNonMatchingType);

            List<Long> questionIds = questionAndAnswersWithNonMatchingType.stream()
                .map(QuestionAndAnswer::answer)
                .map(Answer::questionId)
                .toList();

            throw new ConsultationsException(HttpStatus.BAD_REQUEST, "Wrong answer type for following question ids: " + questionIds);
        }

        record QuestionAnswerValidationResult(Question question, Answer<?> answer, boolean valid) {}

        List<QuestionAnswerValidationResult> validationResults = questionAndAnswers.stream()
            .map(questionAndAnswer -> {
                Question question = questionAndAnswer.question();
                Answer<?> answer = questionAndAnswer.answer();
                boolean valid = validateAnswer(question, answer);
                return new QuestionAnswerValidationResult(question, answer, valid);
            })
            .toList();

        List<QuestionAnswerValidationResult> invalidAnswers = validationResults.stream()
            .filter(questionAnswerValidationResult -> !questionAnswerValidationResult.valid())
            .toList();

        if (!invalidAnswers.isEmpty()) {
            // logging of invalid answers, but not returning to user yet
            log.error("Invalid answers: {}", invalidAnswers);
        }

        ConsultationResponse response = new ConsultationResponse(invalidAnswers.isEmpty() ? Status.REFERRED : ConsultationResponse.Status.FAILED);
        ConsultationResponseDto responseDto = consultationMapper.mapToConsultationResponse(response);

        return ResponseEntity.ok(responseDto);
    }

    private boolean validateAnswer(Question question, Answer<?> answer) {
        AnswerValidator<Answer<?>> answerValidator = (AnswerValidator<Answer<?>>) question.answerValidator();

        // need to check if the validator is of the correct answerType for the answer
        boolean validatorIsApplicableType = answerValidator.isApplicableType(answer);

        if (validatorIsApplicableType) {
            boolean validationResponse = answerValidator.validate(answer);
            log.info("Validation result for question: {} is: {}", question.text(), validationResponse);
            return validationResponse;
        } else {
            // Should never reach this state, so actually an exception would make more sense
            // boolean for now as it's within Stream and exception handling is too painful given time constraints
            // we would validate the answerType of the Validator at the point of creation of the Question
            log.error("Validator is not of the correct answerType for question: {}", question.text());
            return false;
        }
    }

    @Component
    class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            Question ageCheck = Question.builder()
                .id(1L)
                .text("Are you over 18 years old?")
                .answerType(AnswerType.BOOLEAN)
                .answerValidator(new AnswerValidator.BooleanValidator.MustBeTrueValidator())
                .build();

            Question previousReactionCheck = Question.builder()
                .id(2L)
                .text("Have you had a reaction to this medicine before?")
                .answerType(AnswerType.BOOLEAN)
                .answerValidator(new AnswerValidator.BooleanValidator.MustBeFalseValidator())
                .build();

            // Arbitrary example to demonstrate validation of an integer answer
            Question previousMedicineCount = Question.builder()
                .id(3L)
                .text("How many times have you taken this medicine?")
                .answerType(AnswerType.INTEGER)
                .answerValidator(new AnswerValidator.IntegerValidator.MustBeLessThanValidator(3))
                .build();

            List<Question> questions = List.of(ageCheck, previousReactionCheck, previousMedicineCount);

            Consultation consultation = Consultation.builder()
                .id(1L)
                .questions(questions)
                .build();

            List<Consultation> consultations = List.of(consultation);

            consultations.forEach(consultationRepository::save);
        }
    }

}
