package silverpotion.userserver.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class User extends silverpotion.userserver.common.domain.BaseTimeEntity {
    //회원 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    //성별
    @Enumerated(EnumType.STRING)
    private Sex sex;
    //권한
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    //전화번호
    @Column(nullable = false)
    private String phoneNumber;
    //이름
    @Column(nullable = false)
    private String name;
    //생년월일(나이)
    @Column(nullable = false)
    private String birthday;
    //로그인아이디
    private String loginId;
    //비밀번호
    private String password;
    //닉네임
    @Column(nullable = false)
    private String nickName;
    //이메일
    @Column(nullable = false)
    private String email;
    //주소
    @Column(nullable = false)
    private String address;
    //캐시
    private Integer cash;
    //회원탈퇴여부
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DelYN delYN = DelYN.N;
    //피보호자(하나의 유저에 4명의 피부양자가 있으니까 Onetomany
    @OneToMany(mappedBy = "careUser")
    @Builder.Default
    private List<User> dependents = new ArrayList<>();
    //보호자(4명의 유저가 같은 보호자를 가질 수 있으니까 ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="careUser_id")
    private User careUser;

}
