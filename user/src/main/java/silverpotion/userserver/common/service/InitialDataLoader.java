package silverpotion.userserver.common.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import silverpotion.userserver.admin.domain.Admin;
import silverpotion.userserver.admin.domain.AdminRole;
import silverpotion.userserver.admin.repository.AdminRepository;
import silverpotion.userserver.user.domain.DelYN;
import silverpotion.userserver.user.domain.Role;
import silverpotion.userserver.user.domain.Sex;
import silverpotion.userserver.user.domain.User;
import silverpotion.userserver.user.repository.UserRepository;
import silverpotion.userserver.user.service.UserService;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    public InitialDataLoader(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder, AdminRepository adminRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createAccounts();
    }

    public void createAccounts(){

        if(!userRepository.findByLoginIdAndDelYN("user",DelYN.N).isPresent()){
            User user = User.builder()
                    .sex(Sex.FEMALE)
                    .role(Role.USER)
                    .phoneNumber("01000000000")
                    .name("홍진영")
                    .birthday("19500818")
                    .loginId("user")
                    .password(passwordEncoder.encode("12341234"))
                    .nickName("홍진영")
                    .email("trot@naver.com")
                    .address("서울시 동작구 신대방동 101로")
                    .zipcode("01010")
                    .detailAddress("현대아파트 101호")
                    .region("서울시 동작구")
                    .healingPotion(10)
                    .build();

            userRepository.save(user);
        }
        if(!userRepository.findByLoginIdAndDelYN("admin",DelYN.N).isPresent()){
            User admin = User.builder()
                    .sex(Sex.FEMALE)
                    .role(Role.ADMIN)
                    .phoneNumber("01012345678")
                    .name("이재석")
                    .birthday("19990102")
                    .loginId("admin")
                    .password(passwordEncoder.encode("12341234"))
                    .nickName("재스기")
                    .email("mcu@naver.com")
                    .address("서울시 노원구 노원동 101로")
                    .zipcode("011123")
                    .detailAddress("삼성아파트 101호")
                    .region("서울시 노원구")
                    .build();

            userRepository.save(admin);
        }
        if(!userRepository.findByLoginIdAndDelYN("user1",DelYN.N).isPresent()){
            User user1 = User.builder()
                    .sex(Sex.FEMALE)
                    .role(Role.USER)
                    .phoneNumber("01095539812")
                    .name("경수혁")
                    .birthday("20000401")
                    .loginId("user1")
                    .password(passwordEncoder.encode("12341234"))
                    .nickName("캡틴경")
                    .email("captain@naver.com")
                    .address("서울시 관악구 신림동 신림로 ")
                    .zipcode("01011")
                    .detailAddress("레미안 101호")
                    .region("서울시 관악구")
                    .healingPotion(10)
                    .build();
            userRepository.save(user1);
        }

        if(!userRepository.findByLoginIdAndDelYN("user2",DelYN.N).isPresent()){
            User user2 = User.builder()
                    .sex(Sex.FEMALE)
                    .role(Role.USER)
                    .phoneNumber("01022335678")
                    .name("최영일")
                    .birthday("20000401")
                    .loginId("user2")
                    .password(passwordEncoder.encode("12341234"))
                    .nickName("01쨔응")
                    .email("0101@naver")
                    .address("경기도 안양시 안양1번가 ")
                    .zipcode("01012")
                    .detailAddress("자이 101호")
                    .region("경기도 안양시")
                    .build();
            userRepository.save(user2);
        }

        if (adminRepository.count() == 0){
            Admin admin = Admin.builder()
                    .name("이재석")
                    .role(AdminRole.SUPER_ADMIN)
                    .user(userRepository.findByLoginId("admin").get())
                    .build();
            adminRepository.save(admin);
        }
    }
}
