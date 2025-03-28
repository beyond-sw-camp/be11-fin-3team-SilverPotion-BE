package silverpotion.userserver.careRelation.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import silverpotion.userserver.careRelation.domain.CareRelation;
import silverpotion.userserver.careRelation.domain.LinkStatus;
import silverpotion.userserver.careRelation.dtos.CareRelationAcceptOrNotDto;
import silverpotion.userserver.careRelation.dtos.CareRelationCreateDto;
import silverpotion.userserver.careRelation.dtos.CareRelationDisconnectDto;
import silverpotion.userserver.careRelation.dtos.CareRelationListDto;
import silverpotion.userserver.careRelation.repository.CareRelationRepository;
import silverpotion.userserver.user.domain.DelYN;
import silverpotion.userserver.user.domain.User;
import silverpotion.userserver.user.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CareRelationService {
    private final CareRelationRepository careRelationRepository;
    private final UserRepository userRepository;


    public CareRelationService(CareRelationRepository careRelationRepository, UserRepository userRepository) {
        this.careRelationRepository = careRelationRepository;
        this.userRepository = userRepository;
    }

//    1.관계요청 보내기(이때 로그인 아이디는 보호자가 될 유저)
    public void sendCareLink(CareRelationCreateDto dto, String loginId){
        User protector = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다1"));
        User dependent = userRepository.findByPhoneNumberAndDelYN(dto.getPhoneNumber(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다.2"));
        CareRelation careRelation = careRelationRepository.save(dto.toEntityFromCreateDto(protector,dependent));
    }

//    2.내게 온 연결 요청 조회(이때 로그인 아이디는 피보호자가 될 유저)
    public List<CareRelationListDto> checkLink(String loginId){
       User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        System.out.println(user.getName());
       List<CareRelation> receivedList = careRelationRepository.findByDependentIdAndLinkStatus(user.getId(), LinkStatus.PENDING);
       return receivedList.stream().map(c->c.toRecievedListFromEntity()).toList();
    }

//    3.연결 요청 수락 혹은 거절(이때 로그인 아이디는 피보호자가 되는 유저)
    public Map<Long,String> acceptOrNot(CareRelationAcceptOrNotDto dto,String loginId){
        Map<Long,String> map = new HashMap<>();// 키값에 관계요청id 밸류값에 관계를 수락했는지 거절했는지

        CareRelation careRelation = careRelationRepository.findByIdAndLinkStatus(dto.getCareRelationId(),LinkStatus.PENDING).orElseThrow(()->new EntityNotFoundException("관계요청이 없습니다"));
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        User senderUser = userRepository.findByLoginIdAndDelYN(dto.getSenderId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        if(dto.getYesOrNo().equals("yes")){
            careRelation.changeMyStatus(dto.getYesOrNo()); //status가 accepted로 바뀜
            loginUser.getAsDependents().add(careRelation);
            senderUser.getAsProtectors().add(careRelation);
            map.put(careRelation.getId(),"accept");
        } else{
            careRelation.changeMyStatus(dto.getYesOrNo()); //status가 rejected로 바뀜
            map.put(careRelation.getId(),"rejected");
        }

        return map;
    }


//    4.보호자 연결 끊기
    public String disconnect(CareRelationDisconnectDto dto, String loginId){
//        dto의 id는 보호자의 id
//        로그인한 유저는 피보호자
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        User protector = userRepository.findByIdAndDelYN(dto.getId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        String x="";
        List<CareRelation> myRelation = loginUser.getAsDependents();
        for(CareRelation c: myRelation ){
            if(c.getProtector().getId().equals(protector.getId())){
                c.disconnectStatus();
                x=c.getProtector().getName();
            }
        }
        return x; //연결이 끊긴 보호자의 이름을 리턴
    }

//    5.피보호자 연결 끊기
    public String disconnectDependent(CareRelationDisconnectDto dto, String loginId){
//        dto의 id는 보호자의 id, 로그인한 유저는 보호자
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        User dependent = userRepository.findByIdAndDelYN(dto.getId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        String x="";
        List<CareRelation> myRelation = loginUser.getAsProtectors();
        for(CareRelation c: myRelation){
            if(c.getDependent().getId().equals(dependent.getId())){
                c.disconnectStatus();
                x=c.getDependent().getName();
            }
        }
        return x; //연결이 끊긴 보호자의 이름을 리턴
    }



}
