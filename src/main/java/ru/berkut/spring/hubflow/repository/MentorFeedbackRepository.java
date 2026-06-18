package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.MentorFeedback;

import java.util.UUID;

public interface MentorFeedbackRepository
        extends JpaRepository<MentorFeedback, UUID> {
}
