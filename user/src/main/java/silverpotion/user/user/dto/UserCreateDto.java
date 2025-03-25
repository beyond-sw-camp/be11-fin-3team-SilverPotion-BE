package silverpotion.user.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.user.user.domain.User;

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
