package silverpotion.userserver.careRelation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CareRelationListDto {
    //    CareRelation 아이디
    private Long CareRelationId;
    //    나한테 연결 요청을 보낸 상대방아이디와,이름,전화번호임
    private String loginId;
    private String name;
    private String phoneNumber;

}
