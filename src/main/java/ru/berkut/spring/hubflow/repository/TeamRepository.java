package ru.berkut.spring.hubflow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.berkut.spring.hubflow.entity.Team;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    // Команды, одобренные в данной когорте (через cohort_applications)
    @Query("""
        SELECT a.team FROM CohortApplication a
        WHERE a.cohort.id = :cohortId AND a.status = 'APPROVED'
    """)
    List<Team> findApprovedTeamsByCohortId(UUID cohortId);

    // Команды, созданные пользователем или где он состоит
    @Query("""
        SELECT DISTINCT tm.team FROM TeamMember tm
        WHERE tm.user.id = :userId
    """)
    List<Team> findByMemberUserId(UUID userId);

    @Query("""
        SELECT p.team FROM DemoDayParticipant p
        WHERE p.demoDay.id = :demoDayId
    """)
    List<Team> findByDemoDayId(UUID demoDayId);
}
