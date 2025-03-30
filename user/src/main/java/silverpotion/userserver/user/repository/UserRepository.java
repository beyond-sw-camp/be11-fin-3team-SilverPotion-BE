package silverpotion.userserver.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import silverpotion.userserver.user.domain.DelYN;
import silverpotion.userserver.user.domain.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndDelYN(Long id,DelYN delYN);

    Optional<User> findByLoginIdAndDelYN(String loginId, DelYN delYN);

    Optional<User> findByNickNameAndDelYN(String NickName, DelYN delYN);

    Optional<User> findByEmailAndDelYN(String email, DelYN delYN);

    Optional<User> findByPhoneNumberAndDelYN(String phoneNumber, DelYN delYN);

    Optional<User> findByLoginId(String loginId);
}
