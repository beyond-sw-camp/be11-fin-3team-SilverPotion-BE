package silverpotion.postserver.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.gathering.domain.Gathering;
import silverpotion.postserver.gathering.domain.GatheringPeople;
import silverpotion.postserver.gathering.domain.Status;
import silverpotion.postserver.meeting.domain.Meeting;
import silverpotion.postserver.meeting.domain.MeetingParticipant;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting,Long> {
    Optional<Meeting> findByIdAndDelYN(Long id, DelYN delYN);
    List<Meeting> findByGatheringIdAndDelYN(Long gatheringId, DelYN delYN);
    List<Meeting> findByDelYN(DelYN delYN);
    List<Meeting> findByMeetingDateAfterOrMeetingDateEqualsAndMeetingTimeAfterAndDelYN(
            LocalDate date, LocalDate sameDate, LocalTime time, DelYN delYN);
    List<Meeting> findByIdIn(List<Long> ids);
}
