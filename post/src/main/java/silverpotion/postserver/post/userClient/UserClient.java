package silverpotion.postserver.post.userClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import silverpotion.dto.UserMyPageDto;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("silverpotion/user/myprofile")
    UserMyPageDto myProfile(@RequestHeader("X-User-Id")String loginId);

    @PostMapping("silverpotion/user/userId")
    Long getUserIdByLoginId(@RequestParam("loginId") String loginId);
}
