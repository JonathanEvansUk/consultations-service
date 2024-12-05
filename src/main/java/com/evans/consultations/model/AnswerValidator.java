package com.evans.consultations.model;

import com.evans.consultations.model.Answer.BooleanAnswer;
import com.evans.consultations.model.Answer.IntegerAnswer;

sealed public interface AnswerValidator<T extends Answer<?>> {

    default boolean isApplicableType(T answer) {
        return answer.type() == applicableType();
    }

    AnswerType applicableType();

    boolean validate(T answer);

    sealed interface BooleanValidator extends AnswerValidator<BooleanAnswer> {

        default AnswerType applicableType() {
            return AnswerType.BOOLEAN;
        }

        record MustBeTrueValidator() implements BooleanValidator {

            @Override
            public boolean validate(BooleanAnswer answer) {
                return answer.value();
            }
        }

        record MustBeFalseValidator() implements BooleanValidator {

            @Override
            public boolean validate(BooleanAnswer answer) {
                return !answer.value();
            }
        }
    }

    sealed interface IntegerValidator extends AnswerValidator<IntegerAnswer> {

        default AnswerType applicableType() {
            return AnswerType.INTEGER;
        }

        record MustBeLessThanValidator(int threshold) implements IntegerValidator {

            @Override
            public boolean validate(IntegerAnswer answer) {
                return answer.value() < threshold;
            }
        }

        record MustBeGreaterThanValidator(int threshold) implements IntegerValidator {

            @Override
            public boolean validate(IntegerAnswer answer) {
                return answer.value() > threshold;
            }
        }
    }
}
