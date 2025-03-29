package silverpotion.userserver.careRelation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.userserver.careRelation.domain.CareRelation;
import silverpotion.userserver.careRelation.domain.LinkStatus;
import silverpotion.userserver.user.domain.User;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CareRelationCreateDto {
//    상대방(피보호자)의 휴대폰번호
    private String phoneNumber;
//    아래 로그인 아이디와 이름은 의미없다. 제거
    private String loginId;
    private String name;




    public CareRelation toEntityFromCreateDto(User protector, User dependent){
        return CareRelation.builder()
                .dependent(dependent)
                .protector(protector)
                .linkStatus(LinkStatus.PENDING)
                .build();
    }

}
