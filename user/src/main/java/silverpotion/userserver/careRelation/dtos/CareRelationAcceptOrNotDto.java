package silverpotion.userserver.careRelation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CareRelationAcceptOrNotDto {
    private String yesOrNo;
    private Long careRelationId;
//  관계 맺기 요청을 보낸 사람의 아이디(이 아이디는 프론트에서 수신한 CareRelationListDto(내게 온 연결요청 조회)에서 뽑아서 담을 수 있음)
    private String senderId;

}
