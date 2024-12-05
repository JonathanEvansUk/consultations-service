package com.evans.consultations.repository;

import com.evans.consultations.model.Question;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class QuestionRepository {

    private final Map<Long, Question> questionsById = new HashMap<>();

    public void save(Question question) {
        questionsById.put(question.id(), question);
    }

    public Question getById(Long id) {
        return questionsById.get(id);
    }

    public boolean existsById(Long id) {
        return questionsById.containsKey(id);
    }
}
