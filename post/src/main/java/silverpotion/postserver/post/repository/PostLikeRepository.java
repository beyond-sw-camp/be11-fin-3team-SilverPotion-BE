package silverpotion.postserver.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import silverpotion.postserver.post.domain.PostLike;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

}
