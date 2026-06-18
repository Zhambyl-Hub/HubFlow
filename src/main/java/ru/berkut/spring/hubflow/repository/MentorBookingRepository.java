package ru.berkut.spring.hubflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.MentorBooking;

import java.util.List;
import java.util.UUID;

public interface MentorBookingRepository extends JpaRepository<MentorBooking, UUID> {
    List<MentorBooking> findByTeamId(UUID teamId);

    @Query("SELECT b FROM MentorBooking b WHERE b.mentorSlot.mentor.id = :mentorId")
    List<MentorBooking> findByMentorId(UUID mentorId);
}
