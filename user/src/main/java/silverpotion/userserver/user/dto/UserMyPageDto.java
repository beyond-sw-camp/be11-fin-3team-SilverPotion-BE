package silverpotion.userserver.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.userserver.user.domain.User;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserMyPageDto {
    private Long id;
    private String nickName;
    private String name;
    private String email;
    private String sex;
    private String phoneNumber;
    private String birthday;
    private String address;
    private String streetAddress;
    private String detailAddress;
    private Integer cash;
    //아래는 나와 연결된 피보호자와 보호자 이름들
    private List<String> dependentName;
    private List<String> protectorName;

}
