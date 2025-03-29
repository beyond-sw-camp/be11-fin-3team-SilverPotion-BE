package silverpotion.userserver.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserUpdateDto {
    private String email;
    private String phoneNumber;
    private String nickName;
    private String password;
    private String address;
    private String streetAddress;
    private String detailAddress;








}


