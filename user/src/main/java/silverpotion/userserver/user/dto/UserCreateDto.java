package silverpotion.userserver.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.userserver.user.domain.User;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreateDto {

    private String name;
    private String email;
    private String password;

    public User toEntity(String encodedPassword){
        return User.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword).build();
    }
}
