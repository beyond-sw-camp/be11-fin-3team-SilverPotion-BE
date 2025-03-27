package silverpotion.userserver.user.controller;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.userserver.common.auth.JwtTokenProvider;
import silverpotion.userserver.common.dto.CommonDto;
import silverpotion.userserver.user.dto.*;
import silverpotion.userserver.user.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("silverpotion/user")
public class UserController {
   private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;

    }
//    0.헬스체크용 url(배포용)
    @GetMapping
    public ResponseEntity<?> healthCheck(){
        return new ResponseEntity<>("transmission sucess",HttpStatus.OK);
    }

//    1.회원가입
    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@RequestBody UserCreateDto dto){
       Long id =userService.userCreate(dto);
       return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(),"user is created successfully",id), HttpStatus.CREATED);
    }

//    2-1.로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto){
        Map<String, Object> loginInfo = userService.login(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"login success", loginInfo),HttpStatus.OK);
    }

//    2-2.로그인(리프레시 토큰 발급)
    @PostMapping("/refresh-token")
    public ResponseEntity<?> recreateAccessToken(@RequestBody UserRefreshDto dto){
        Map<String,Object> loginInfo = userService.recreateAccessToken(dto);
        if(loginInfo.get("token").equals("fail")){
            return new ResponseEntity<>(new CommonDto(HttpStatus.BAD_REQUEST.value(), "cannot recreate accessToken",loginInfo),HttpStatus.BAD_REQUEST);
        } else{
            return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "success",loginInfo),HttpStatus.CREATED);
        }
    }

//    3.회원정보수정(마이프로필 수정)
    @PatchMapping("/update")
    public ResponseEntity<?> userUpdate(@RequestBody UserUpdateDto dto,@RequestHeader("X-User-Id")String loginId){
        Long id = userService.update(dto,loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "update success", id),HttpStatus.OK);
    }

// 4.내 정보 조회(마이페이지 프로필 조회)
//    @GetMapping("/myprofile")
//    public ResponseEntity<?> myProfile(@RequestBody UserProfileDto dto, @RequestHeader("X-User_Id")String loginId){
//        Long id = userService.myProfile(dto,loginId);
//   //잠시 중단
//    }

//   5. 피보호자 연결 요청(건강메이트?)
//    @Post

}
