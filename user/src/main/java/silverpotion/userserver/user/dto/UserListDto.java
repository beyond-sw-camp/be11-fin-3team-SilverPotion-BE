package silverpotion.userserver.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.userserver.user.domain.User;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserListDto {
    private Long id;
    private String name;
    private String nickName;
    private String profileImgUrl;
  
  
    public static UserListDto fromEntity(User user) {
        return new UserListDto(user.getId(), user.getName(), user.getNickName(), user.getProfileImage());
    }
}
