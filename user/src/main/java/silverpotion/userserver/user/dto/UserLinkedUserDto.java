package silverpotion.userserver.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
//나와 피보호자연결된사람 혹은 보호자로 연결된 사람 조회용 dto
public class UserLinkedUserDto {
//   보호자조회에선 보호자아이디, 피보호자 조회에선 피보호자 아이디
    private Long userId;
    private String name;
}
