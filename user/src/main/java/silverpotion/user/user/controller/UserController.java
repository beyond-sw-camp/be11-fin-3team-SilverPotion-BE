package silverpotion.user.user.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.user.common.dto.CommonDto;
import silverpotion.user.user.dto.UserCreateDto;
import silverpotion.user.user.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
   private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@RequestBody UserCreateDto dto){
        userService.userCreate(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(),"user create successful","OK"), HttpStatus.CREATED);
    }
}
