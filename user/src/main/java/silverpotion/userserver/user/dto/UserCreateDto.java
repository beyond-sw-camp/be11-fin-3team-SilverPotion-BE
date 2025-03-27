package silverpotion.userserver.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.userserver.user.domain.Sex;
import silverpotion.userserver.user.domain.SocialType;
import silverpotion.userserver.user.domain.User;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserCreateDto {

   //OauthLogin위해 nullable
    private String loginId;
    @Size(max = 10)
    @NotBlank(message = "이름을 입력하세요")
    private String name;
    @NotEmpty(message = "성별을 선택하세요")
    private Sex sex;
    @Size(max = 50)
    @NotBlank(message = "이메일을 입력하세요")
    private String email;
    @NotBlank(message = "전화번호를 입력하세요")
    private String phoneNumber;
    @NotBlank(message = "생일을 입력하세요")
    private String birthday;
    @Size(max =9)
    @NotBlank(message = "닉네임을 입력하세요(8자 이내)")
    private String nickName;
    //OauthLogin위해 nullable
    private String password;
    @NotBlank(message = "주소를 입력하세요")
    private String address;
    private String streetAddress;
    private String detailAddress;
    @Builder.Default
    private SocialType socialType =SocialType.NONE;
    private String socialId;

    public User toEntity(String encodedPassword){
        return User.builder().loginId(this.loginId).name(this.name).sex(this.sex)
                .email(this.email).phoneNumber(this.phoneNumber).birthday(this.birthday)
                .nickName(this.nickName).password(encodedPassword).address(this.address)
                .streetAddress(this.streetAddress).detailAddress(this.detailAddress)
                .socialType(this.socialType).socialId(this.socialId)
                .build();

    }
}
