package com.evans.consultations.repository;

import com.evans.consultations.model.Consultation;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultationRepository {

    private final Map<Long, Consultation> consultationsById = new HashMap<>();

    public void save(Consultation consultation) {
        consultationsById.put(consultation.id(), consultation);
    }

    public Consultation getById(Long id) {
        return consultationsById.get(id);
    }

    public boolean existsById(Long id) {
        return consultationsById.containsKey(id);
    }

    public void deleteAll() {
        consultationsById.clear();
    }
}
