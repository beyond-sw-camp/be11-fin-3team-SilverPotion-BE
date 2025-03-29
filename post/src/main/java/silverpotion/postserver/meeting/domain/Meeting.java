package silverpotion.postserver.meeting.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.gathering.domain.Gathering;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;    //모임id

    @Column(nullable = false)
    private String gatheringName;   //모임명

    @Column(nullable = false)
    private LocalDate meetingDate;  // 날짜 ex) 2025-03-26

    @Column(nullable = false)
    private LocalTime meetingTime;  // 시간 ex) 14:30:00

    @Column(nullable = false)
    private String place;   //장소

    @Column
    private String imageUrl;    //사진

    @Column(nullable = false)
    private Long cost;  //비용

    @Column(nullable = false)
    private Long maxPeople;    //정원

    @Builder.Default
    @Enumerated(EnumType.STRING )
    private DelYN delYN=DelYN.N;    //삭제여부
}
