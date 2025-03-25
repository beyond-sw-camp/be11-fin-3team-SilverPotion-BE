package silverpotion.userserver.user.service;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import silverpotion.userserver.user.repository.UserRepository;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
