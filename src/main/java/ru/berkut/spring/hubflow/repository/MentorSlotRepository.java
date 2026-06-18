package ru.berkut.spring.hubflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.berkut.spring.hubflow.entity.MentorSlot;
import ru.berkut.spring.hubflow.enums.SlotStatus;

import java.util.List;
import java.util.UUID;

public interface MentorSlotRepository extends JpaRepository<MentorSlot, UUID> {
    List<MentorSlot> findByCohortIdAndStatus(UUID cohortId, SlotStatus status);
    List<MentorSlot> findByMentorIdAndCohortId(UUID mentorId, UUID cohortId);
}
