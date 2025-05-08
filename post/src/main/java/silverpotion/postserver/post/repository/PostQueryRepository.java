package silverpotion.postserver.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import silverpotion.postserver.post.domain.Post;
import silverpotion.postserver.post.dtos.PostVoteUnionDto;

import java.util.List;

@Repository
public interface PostQueryRepository extends JpaRepository<Post, Long> {
    @Query(value = """
        (SELECT 
            p.id AS id,
            p.title AS title,
            p.content AS content,
            p.created_time AS createdAt,
            p.view_count AS viewCount,
            NULL AS multipleChoice,
            NULL AS voteOptions,
            p.post_category AS postCategory,
            'POST' AS postType
         FROM post p
         WHERE p.del_yn = 'N' AND p.post_status = 'fin' AND p.gathering_id = :gatheringId)

        UNION ALL

        (SELECT 
            v.vote_id AS id,
            v.title AS title,
            v.description AS content, 
            v.created_time AS createdAt,
            NULL AS viewCount,
            v.multiple_choice AS multipleChoice,
            NULL AS voteOptions,
            v.post_category AS postCategory,
            'VOTE' AS postType
         FROM vote v
         WHERE v.del_yn = 'N' AND v.post_status = 'fin' AND v.gathering_id = :gatheringId)

        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<PostVoteUnionDto> findAllPostAndVote(@Param("gatheringId") Long gatheringId,@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
    SELECT (
        (SELECT COUNT(*) FROM post p WHERE p.del_yn = 'N' AND p.post_status = 'normal')
        +
        (SELECT COUNT(*) FROM vote v WHERE v.del_yn = 'N' AND v.post_status = 'normal')
    )
    """, nativeQuery = true)
    long countPostAndVote();
}
