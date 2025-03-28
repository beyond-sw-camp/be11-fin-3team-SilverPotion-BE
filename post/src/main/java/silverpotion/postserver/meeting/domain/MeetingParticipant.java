package silverpotion.postserver.meeting.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.postserver.common.domain.BaseTimeEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MeetingParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;    //정모id

    @Column(nullable = false)
    private Long userId;    //회원id
}
