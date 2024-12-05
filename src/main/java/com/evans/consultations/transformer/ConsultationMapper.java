package com.evans.consultations.transformer;

import com.evans.consultations.model.Answer;
import com.evans.consultations.model.Answer.BooleanAnswer;
import com.evans.consultations.model.Answer.IntegerAnswer;
import com.evans.consultations.model.AnswerDto;
import com.evans.consultations.model.BooleanAnswerDto;
import com.evans.consultations.model.Consultation;
import com.evans.consultations.model.ConsultationDto;
import com.evans.consultations.model.ConsultationResponse;
import com.evans.consultations.model.ConsultationResponseDto;
import com.evans.consultations.model.IntegerAnswerDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {

    ConsultationDto mapToConsultation(Consultation consultation);

    List<Answer<?>> mapToAnswers(List<AnswerDto> answers);

    default Answer<?> mapToAnswer(AnswerDto answerDto) {
        // would use switch pattern matching here in newer versions of java
        if (answerDto instanceof BooleanAnswerDto booleanAnswerDto) {
            return mapToBooleanAnswer(booleanAnswerDto);
        }

        if (answerDto instanceof IntegerAnswerDto integerAnswerDto) {
            return mapToIntegerAnswer(integerAnswerDto);
        }

        return null;
    }

    BooleanAnswer mapToBooleanAnswer(BooleanAnswerDto answer);

    IntegerAnswer mapToIntegerAnswer(IntegerAnswerDto answer);

    ConsultationResponseDto mapToConsultationResponse(ConsultationResponse response);
}
