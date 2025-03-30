package silverpotion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    private Long id;
    private String sex;
    private String role;
    private String phoneNumber;
    private String name;
    private String birthday;
    private String loginId;
    private String password;
    private String nickName;
    private String email;
    private String address;
    private String streetAddress;
    private String detailAddress;
    private String cash;
    private String delYN;
    private String asDependents;
    private String asProtectors;
    private String socialType;
    private String socialId;

}
