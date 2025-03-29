package silverpotion.userserver.user.domain;

import jakarta.persistence.*;
import lombok.*;
import silverpotion.userserver.careRelation.domain.CareRelation;
import silverpotion.userserver.user.dto.UserCreateDto;
import silverpotion.userserver.user.dto.UserLinkedUserDto;
import silverpotion.userserver.user.dto.UserMyPageDto;
import silverpotion.userserver.user.dto.UserUpdateDto;

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
    private Long id;
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
    //주소(우편번호)
    @Column(nullable = false)
    private String address;
    //주소(지번주소)
    @Column(nullable = false)
    private String streetAddress;
    //주소(상세주소)
    @Column(nullable = false)
    private String detailAddress;
    //캐시
    private Integer cash;
    //회원탈퇴여부
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DelYN delYN = DelYN.N;
    //내가 피보호자로 들어가는 관계
    @OneToMany(mappedBy = "dependent")
    @Builder.Default
    private List<CareRelation> asDependents = new ArrayList<>();
    //내가 보호자로 들어가는 관계
    @OneToMany(mappedBy = "protector")
    @Builder.Default
    private List<CareRelation> asProtectors = new ArrayList<>();
    //로그인 타입
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    //소셜 로그인 아이디
    private String socialId;


    public void updateUser(UserUpdateDto dto,String newPw){
        if(dto.getEmail() != null){
            this.email = dto.getEmail();
        }
        if(dto.getPhoneNumber() != null){
            this.phoneNumber = dto.getPhoneNumber();
        }
        if(dto.getNickName() != null){
            this.nickName = dto.getNickName();
        }
        if(dto.getPassword() != null){
            this.password = newPw;
        }
        if(dto.getAddress() != null){
            this.address = dto.getAddress();
        }
        if(dto.getStreetAddress() != null){
            this.streetAddress = dto.getStreetAddress();
        }
        if(dto.getDetailAddress() != null){
            this.detailAddress = dto.getDetailAddress();
        }
    }

    public UserMyPageDto toMyPageDtoFromEntity(List<String> dependentNames, List<String>protectorNames){
        return UserMyPageDto.builder().nickName(this.nickName).name(this.name).email(this.email)
                .sex(this.sex.toString()).phoneNumber(this.phoneNumber).birthday(this.birthday)
                .address(this.address).streetAddress(this.streetAddress).detailAddress(this.detailAddress)
                .cash(this.cash).id(this.id)
                .dependentName(dependentNames)
                .protectorName(protectorNames)
                .build();
    }
    public UserLinkedUserDto toLinkUserDtoFromEntity(){
        return UserLinkedUserDto.builder().userId(this.id).name(this.name).build();
    }

    public List<String> findNameFromDependentList(){
       List<CareRelation> dependents = this.asProtectors;
       List<String> dependentNames = new ArrayList<>();
       for(CareRelation c : dependents){
          dependentNames.add(c.getDependent().getName());
       }
       return dependentNames;
    }

    public List<String> findNameFromProtectorsList(){
        List<CareRelation> protectors = this.asDependents;
        List<String> protectorNames = new ArrayList<>();
        for(CareRelation c : protectors){
            protectorNames.add(c.getProtector().getName());
        }
        return protectorNames;
    }




}
