package silverpotion.user.user.domain;

import jakarta.persistence.*;
import lombok.*;
import silverpotion.user.user.dto.UserListDto;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class User extends silverpotion.user.common.domain.BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Enumerated(EnumType.STRING)
    private Sex sex;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    private String phoneNumber;
    private String name;
    private String birthday;
    private String password;
    private String nickName;
    private String email;
    private String adresse;
    private DelYN delYN = DelYN.N;

}
