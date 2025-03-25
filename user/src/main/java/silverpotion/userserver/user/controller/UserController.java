package silverpotion.userserver.user.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.userserver.common.dto.CommonDto;
import silverpotion.userserver.user.dto.UserCreateDto;
import silverpotion.userserver.user.service.UserService;

@RestController
@RequestMapping("silverpotion/user")
public class UserController {
   private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @PostMapping("/create")
//    public ResponseEntity<?> userCreate(@RequestBody UserCreateDto dto){
//        userService.userCreate(dto);
//        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(),"user create successful","OK"), HttpStatus.CREATED);
//    }
}
