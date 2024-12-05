package com.evans.consultations.model;

import lombok.Builder;

@Builder
public record Question(
    Long id,
    String text,
    AnswerType answerType,
    AnswerValidator<? extends Answer<?>> answerValidator
) {

}
