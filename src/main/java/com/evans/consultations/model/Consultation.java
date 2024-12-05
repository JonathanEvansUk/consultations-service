package com.evans.consultations.model;

import java.util.List;
import lombok.Builder;

@Builder
public record Consultation(
    Long id,
    List<Question> questions
) {

}
