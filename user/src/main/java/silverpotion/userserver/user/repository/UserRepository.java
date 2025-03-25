package silverpotion.userserver.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import silverpotion.userserver.user.domain.DelYN;
import silverpotion.userserver.user.domain.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndDelYN(String email, DelYN delYN);
}
