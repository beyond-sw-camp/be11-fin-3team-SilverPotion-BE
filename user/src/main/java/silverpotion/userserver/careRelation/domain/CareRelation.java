package silverpotion.userserver.careRelation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.userserver.careRelation.dtos.CareRelationListDto;
import silverpotion.userserver.common.domain.BaseTimeEntity;
import silverpotion.userserver.user.domain.User;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class CareRelation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protector_id")
    private User protector;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_id")
    private User dependent;
    @Enumerated(EnumType.STRING)
    private LinkStatus linkStatus;


//  승인하면 연결 거부하면 연결거부
    public void changeMyStatus(String yesOrNo){
        if(yesOrNo.equals("yes")){
            this.linkStatus = LinkStatus.CONNECTED;
        } else if(yesOrNo.equals("no")){
            this.linkStatus =LinkStatus.REJECTED;
        }
    }

//    연결끊기
    public void disconnectStatus(){
        this.linkStatus=LinkStatus.REJECTED;
    }


//
    public CareRelationListDto toRecievedListFromEntity(){
        return CareRelationListDto.builder()
                .CareRelationId(this.id)
                .loginId(protector.getLoginId())
                .phoneNumber(protector.getPhoneNumber())
                .name(protector.getName())
                .build();
    }

//



}
