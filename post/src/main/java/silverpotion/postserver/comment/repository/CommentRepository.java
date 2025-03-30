package silverpotion.postserver.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import silverpotion.postserver.comment.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
}
