package silverpotion.userserver.careRelation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CareRelationDisconnectDto {
    //연결을 끊을 보호자의 id
    private Long id;



}
