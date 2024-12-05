package com.evans.consultations.model;

public sealed interface Answer<T> {

    AnswerType type();

    Long questionId();

    T value();

    record BooleanAnswer(
        Long questionId,
        Boolean value
    ) implements Answer<Boolean> {

        @Override
        public AnswerType type() {
            return AnswerType.BOOLEAN;
        }
    }

    record IntegerAnswer(
        Long questionId,
        Integer value
    ) implements Answer<Integer> {

        @Override
        public AnswerType type() {
            return AnswerType.INTEGER;
        }
    }

}
