package silverpotion.userserver.user.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.userserver.common.auth.JwtTokenProvider;
import silverpotion.userserver.common.dto.CommonDto;
import silverpotion.userserver.payment.dtos.CashItemOfPaymentListDto;
import silverpotion.userserver.user.domain.User;
import silverpotion.userserver.user.dto.*;
import silverpotion.userserver.user.service.GoogleService;
import silverpotion.userserver.user.service.KakaoService;
import silverpotion.userserver.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("silverpotion/user")
public class    UserController {
   private final UserService userService;
   private final GoogleService googleService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoService kakaoService;


    public UserController(UserService userService, GoogleService googleService, JwtTokenProvider jwtTokenProvider, KakaoService kakaoService) {
        this.userService = userService;
        this.googleService = googleService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.kakaoService = kakaoService;
    }
//    0.헬스체크용 url(배포용)
    @GetMapping("/healthcheck")
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
    public ResponseEntity<?> userUpdate(@RequestBody UserUpdateDto dto,@RequestHeader("X-User-LoginId")String loginId){
        Long id = userService.update(dto,loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "update success", id),HttpStatus.OK);
    }

// 4.내 정보 조회(마이페이지 프로필 조회,여기에 연결된 보호자,피보호자이름도 조회할 수 있게 해놨는데 프론트에서는 아래 5,6번 쓰는게 더 나을 듯)
    @GetMapping("/myprofile")
    public ResponseEntity<?> myProfile(@RequestHeader("X-User-LoginId")String loginId){
        UserMyPageDto dto = userService.userMyPage(loginId);
    return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "success",dto),HttpStatus.OK);
    }

//   5. 연결된 피보호자 조회
    @GetMapping("/myDependentList")
    public ResponseEntity<?> whoMyDependents(@RequestHeader("X-User-LoginId")String loginId){
      List<UserLinkedUserDto>dependents = userService.whoMyDependents(loginId);
    return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"success",dependents ),HttpStatus.OK);
    }

//    6.연결된 보호자 조회
    @GetMapping("/myProtectList")
    public ResponseEntity<?> whoMyProtectors(@RequestHeader("X-User-LoginId")String loginId){
        List<UserLinkedUserDto>protectors = userService.whoMyProtectors(loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"success",protectors),HttpStatus.OK);
    }

    //    7.userId 조회(UserClient)
    @GetMapping("/userId")
    public Long getUserIdByLoginId(@RequestParam String loginId){
        return userService.getUserIdByLoginId(loginId);
    }

    //    8.loginId와 nickname 조회(UserClient)
    @GetMapping("/postUserInfo")
    public UserProfileInfoDto getUserProfileInfo(@RequestParam String loginId){
        return userService.getUserProfileInfo(loginId);
    }

    //    9.userId와 nickname 조회(UserClient)
    @GetMapping("/writer/postUserInfo")
    public UserProfileInfoDto getUserProfileInfo(@RequestParam Long userId){
        return userService.getUserProfileInfo(userId);
    }

    //  10. user list 전체 조회
    @GetMapping("/list")
    public ResponseEntity<?> findAllUser(UserListDto dto) {
        List<UserListDto> list = userService.findAll(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "userList is uploaded successfully", list), HttpStatus.OK);
    }
    // Feign chat. idByNickname (Feign 용)
    @GetMapping("/nickname")
    public String getUserByLoginId(@RequestParam Long id) {
        User user = userService.getUseridByNickName(id);
        System.out.println("유저 정보조회 login ID:" + user.getNickName());
        return user.getNickName();
    }

    // 11. 프로필 이미지 등록 및 수정
    @PostMapping("/profileImg")
    public ResponseEntity<?> postProfileImage(@RequestHeader("X-User-LoginId")String loginId,UserProfileImgDto dto){
         String s3Url = userService.postProfileImage(loginId,dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "sucess",s3Url),HttpStatus.OK);
    }

   // 12. 상대프로필 조회
    @GetMapping("/yourProfile/{id}" )
    public ResponseEntity<?> yourProfile(@PathVariable Long id){
                  UserProfileInfoDto dto = userService.yourProfile(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"user's profile is uploaded successfully",dto),HttpStatus.OK);
    }

    // 13. 특정 유저 리스트 조회
    @PostMapping("/profile/list")
    public ResponseEntity<?> getUsersByIds(@RequestBody List<Long> userIds){
        List<UserListDto> userListDtos = userService.getUsersByIds(userIds);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"특정 유저 리스트 조회",userListDtos),HttpStatus.OK);
    }

    //14. 내 결제내역 조회하기
    @GetMapping("/mypayment")
    public ResponseEntity<?> getMyPayments(@RequestHeader("X-User-LoginId")String loginId){
                 List<CashItemOfPaymentListDto> list = userService.getMyPayments(loginId);
                 return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"success",list),HttpStatus.OK);
    }

//    게시물 조회시, 작성자 프로필 조회
    @PostMapping("/post/profileInfo")
    public ResponseEntity<?> PostProfileInfo(@RequestBody List<Long> userIds){
        Map<Long, UserProfileInfoDto> dto= userService.getProfileInfoMap(userIds);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"게시물 작성자 유저 리스트 조회",dto),HttpStatus.OK);
    }

//    사용자 정지(관리자 수동 처리)
    //    @PreAuthorize("hasRole('ADMIN))
    @PostMapping("/ban")
    public ResponseEntity<?> banUser(@RequestBody UserBanRequestDto userBanRequestDto){
        userService.banUserManually(userBanRequestDto.getUserId(),userBanRequestDto.getBanUntil());
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"사용자가 정지되었습니다.",userBanRequestDto.getUserId()),HttpStatus.OK);
    }

//    구글 로그인
    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto){
//        access토큰발급
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());

//        사용자 정보 얻기
        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());
//        회원가입이 되어있지 않다면 회원가입
        User originalUser = userService.userBySocialId(googleProfileDto.getSub());
        System.out.println(googleProfileDto.getSub());
        System.out.println(originalUser);
        if(originalUser == null){
            SocialSignUpDto signUpDto = new SocialSignUpDto(
                    googleProfileDto.getSub(),
                    googleProfileDto.getEmail(),
                    googleProfileDto.getName()
            );
            return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "need_sign_up",signUpDto),HttpStatus.OK);
        }

//        회원가입 되어있으면 토큰 발급
        else {
            String jwtToken = jwtTokenProvider.createToken(originalUser.getLoginId(),originalUser.getRole().toString());
            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("id",originalUser.getId());
            loginInfo.put("token", jwtToken);
            return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"success",loginInfo),HttpStatus.OK);
        }
    }
    //    구글 로그인
    @PostMapping("/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto){
//        access토큰발급
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());

//        사용자 정보 얻기
        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
//        회원가입이 되어있지 않다면 회원가입
        User originalUser = userService.userBySocialId(kakaoProfileDto.getId());
        if(originalUser == null){
            KakaoSignUpDto signUpDto = new KakaoSignUpDto(
                    kakaoProfileDto.getId(),
                    kakaoProfileDto.getKakao_account().getEmail(),
                    kakaoProfileDto.getKakao_account().getProfile().getNickname()
            );
            return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "need_sign_up",signUpDto),HttpStatus.OK);
        }

//        회원가입 되어있으면 토큰 발급
        else {
            String jwtToken = jwtTokenProvider.createToken(originalUser.getLoginId(),originalUser.getRole().toString());
            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("id",originalUser.getId());
            loginInfo.put("token", jwtToken);
            return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"success",loginInfo),HttpStatus.OK);
        }
    }

    //  비밀번호 변경
    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(@RequestHeader("X-User-LoginId")String loginId,@RequestBody ChangePasswordDto dto){
        Long userId = userService.changePassword(loginId,dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "password is changed",userId),HttpStatus.OK);
    }

    // 회원탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("X-User-LoginId")String loginId){
       String nickName = userService.withdraw(loginId);
       return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "goodbye...",nickName),HttpStatus.OK);
    }

}
