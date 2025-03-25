package silverpotion.user.user.service;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import silverpotion.user.user.dto.UserCreateDto;
import silverpotion.user.user.repository.UserRepository;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
