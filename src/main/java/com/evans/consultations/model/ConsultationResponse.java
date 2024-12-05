package com.evans.consultations.model;

public record ConsultationResponse(Status status) {

    public enum Status {
        FAILED,
        REFERRED
    }
}
