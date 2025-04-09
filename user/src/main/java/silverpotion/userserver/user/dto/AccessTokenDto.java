package silverpotion.userserver.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenDto {
    private String access_token;
    private String expires_in;
    private String scope;
    private String id_token;
}
