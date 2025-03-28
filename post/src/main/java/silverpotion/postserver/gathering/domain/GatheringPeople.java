package silverpotion.postserver.gathering.domain;

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
public class GatheringPeople extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;    //모임id

    @Column(nullable = false)
    private Long userId;    //회원id

    @Column(nullable = false)
    private String greetingMessage;    //가입인사

    @Enumerated(EnumType.STRING)
    private Status status;      //상태
}
