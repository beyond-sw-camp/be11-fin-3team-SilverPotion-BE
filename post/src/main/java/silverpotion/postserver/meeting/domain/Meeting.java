package silverpotion.postserver.meeting.domain;

import jakarta.persistence.*;
import lombok.*;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.gathering.domain.Gathering;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;    //모임id

    @Column(nullable = false)
    private String name;   //모임명

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

    @Column(nullable = false)
    private Double lat;    //위도

    @Column(nullable = false)
    private Double lon;    //경도

    @Builder.Default
    @Enumerated(EnumType.STRING )
    private DelYN delYN=DelYN.N;    //삭제여부
}
