package silverpotion.userserver.careRelation.controller;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.userserver.careRelation.dtos.CareRelationAcceptOrNotDto;
import silverpotion.userserver.careRelation.dtos.CareRelationCreateDto;
import silverpotion.userserver.careRelation.dtos.CareRelationDisconnectDto;
import silverpotion.userserver.careRelation.dtos.CareRelationListDto;
import silverpotion.userserver.careRelation.service.CareRelationService;
import silverpotion.userserver.common.dto.CommonDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("silverpotion/carelink")
public class CareRelationController {
    private final CareRelationService careRelationService;

    public CareRelationController(CareRelationService careRelationService) {
        this.careRelationService = careRelationService;
    }

    // 1.연결 요청 보내기
//    이때 로그인 아이디는 연결을 보내는 사람이니까 보호자가 될 유저.
    @PostMapping("/send")
    public ResponseEntity<?> sendCareLink(@RequestBody CareRelationCreateDto dto, @RequestHeader("X-User-LoginId")String loginId){
        careRelationService.sendCareLink(dto,loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "sending success","success"),HttpStatus.CREATED);
    }

    // 2.내가 받은 연결 요청 조회
//    이때 로그인 아이디는 요청을 받은 사람이니까 피보호자가 될 유저
    @GetMapping("/checkLink")
    public ResponseEntity<?> checkLink(@RequestHeader("X-User-LoginId")String loginId){
        List<CareRelationListDto> list = careRelationService.checkLink(loginId);
        return new ResponseEntity(new CommonDto(HttpStatus.OK.value(), "list is uploaded successfully",list),HttpStatus.OK);
    }

    // 3.연결 요청 수락 혹은 거절
//    이때 로그인 아이디는 요청을 받은 사람이니까 피보호자가 될 유저
    @PostMapping("/chooseRelation")
    public ResponseEntity<?> acceptOrNot(@RequestBody CareRelationAcceptOrNotDto dto, @RequestHeader("X-User-LoginId")String loginId){
        Map<Long,String> info = careRelationService.acceptOrNot(dto,loginId);
        if(info.get(dto.getCareRelationId()).equals("accept")){
            return new ResponseEntity(new CommonDto(HttpStatus.OK.value(), "link success",dto.getCareRelationId()),HttpStatus.OK);
        } else{
            return new ResponseEntity(new CommonDto(HttpStatus.OK.value(), "link failed",dto.getCareRelationId()),HttpStatus.OK);
        }

    }

    // 4.보호자 연결 끊기
    @PostMapping("/disconnectProtector")
    public ResponseEntity<?> disconnect(@RequestBody CareRelationDisconnectDto dto,@RequestHeader("X-User-LoginId")String loginId){
        String protectorName =  careRelationService.disconnect(dto,loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"relation is disconnected",protectorName),HttpStatus.OK);
    }

    //  5.피보호자 연결 끊기
    @PostMapping("/disconnectDependent")
    public ResponseEntity<?> disconnectDependent(@RequestBody CareRelationDisconnectDto dto,@RequestHeader("X-User-LoginId")String loginId){
        String dependentName =  careRelationService.disconnectDependent(dto,loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"relation is disconnected",dependentName),HttpStatus.OK);
    }




}
