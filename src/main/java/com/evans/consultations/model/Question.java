package com.evans.consultations.model;

import lombok.Builder;

@Builder
public record Question<T extends Answer<?>>(
    Long id,
    String text,
    AnswerType answerType,
    AnswerValidator<T> answerValidator
) {

}
