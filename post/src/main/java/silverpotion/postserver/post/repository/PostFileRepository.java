package silverpotion.postserver.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import silverpotion.postserver.post.domain.PostFile;

@Repository
public interface PostFileRepository extends JpaRepository<PostFile,Long> {

}
