package silverpotion.postserver.comment.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import silverpotion.postserver.comment.domain.Comment;
import silverpotion.postserver.comment.domain.CommentLike;
import silverpotion.postserver.comment.dtos.*;
import silverpotion.postserver.comment.repository.CommentLikeRepository;
import silverpotion.postserver.comment.repository.CommentRepository;
import silverpotion.postserver.notification.dto.NotificationMessageDto;
import silverpotion.postserver.notification.service.NotificationProducer;
import silverpotion.postserver.post.feignClient.UserClient;
import silverpotion.postserver.post.domain.Post;
import silverpotion.postserver.post.domain.Vote;
import silverpotion.postserver.post.dtos.UserListDto;
import silverpotion.postserver.post.dtos.UserProfileInfoDto;
import silverpotion.postserver.post.repository.PostRepository;
import silverpotion.postserver.post.repository.VoteRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static silverpotion.postserver.common.config.RabbitMQConfig.BACKUP_QUEUE_COMMENT_AL;
import static silverpotion.postserver.common.config.RabbitMQConfig.BACKUP_QUEUE_COMMENT_ML;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserClient userClient;
    private final CommentLikeRepository commentLikeRepository;
    private final RabbitTemplate rabbitTemplate;
    @Qualifier("commentLikeRedisTemplate")
    private final RedisTemplate<String, Object> commentLikeRedisTemplate;
    private final VoteRepository voteRepository;
    private final NotificationProducer notificationProducer;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserClient userClient, CommentLikeRepository commentLikeRepository, RabbitTemplate rabbitTemplate, @Qualifier("commentLikeRedisTemplate") RedisTemplate<String, Object> commentLikeRedisTemplate, VoteRepository voteRepository, NotificationProducer notificationProducer) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userClient = userClient;
        this.commentLikeRepository = commentLikeRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.commentLikeRedisTemplate = commentLikeRedisTemplate;
        this.voteRepository = voteRepository;
        this.notificationProducer = notificationProducer;
    }

    public Long commentCreate(String loginId, CommentCreateDto commentCreateDto){
        UserProfileInfoDto userProfileInfoDto = userClient.getUserProfileInfo(loginId);
        Post post = null;
        Vote vote = null;
        if (commentCreateDto.getPostId() != null) {
            post = postRepository.findById(commentCreateDto.getPostId()).orElseThrow(()->new EntityNotFoundException("Post not found"));
        } else if (commentCreateDto.getVoteId() != null) {
            vote = voteRepository.findById(commentCreateDto.getVoteId()).orElseThrow(()->new EntityNotFoundException("Vote not found"));
        }
        else {
            throw new IllegalArgumentException("PostId and VoteId cannot be null");
        }
        Comment comment = Comment.builder()
                .userId(userProfileInfoDto.getUserId())
                .post(post)
                .vote(vote)
                .content(commentCreateDto.getContent())
                .build();

        commentRepository.save(comment);

        //댓글 알림발송
        String writerLoginId = null;
        Long referenceId = null;
        if(post != null){
            writerLoginId = userClient.getNicknameByUserId(post.getWriterId());
            referenceId = post.getId();
        }
        else if(vote != null){
            writerLoginId = userClient.getNicknameByUserId(vote.getWriterId());
            referenceId = vote.getVoteId();
        }
        NotificationMessageDto notification = NotificationMessageDto.builder()
                .loginId(writerLoginId)
                .title("댓글 알림")
                .content("'" + userProfileInfoDto.getNickname() + "'님이 회원님의 게시글에 댓글을 달았습니다.")
                .type("VOTE_LIKED")
                .referenceId(referenceId)
                .build();

        notificationProducer.sendNotification(notification);
        // PostId가 null일 수 있으니 반환도 조정 필요
        return (post != null) ? post.getId() : vote.getVoteId();
    }

    public Long commentUpdate(String loginId, CommentUpdateDto commentUpdateDto) {
        Long userId = userClient.getUserIdByLoginId(loginId);
        Comment comment = commentRepository.findById(commentUpdateDto.getCommentId()).orElseThrow(() -> new EntityNotFoundException("댓글이 없습니다."));

        if (!(userId.equals(comment.getUserId()))) {
            return 0L;
        }
        comment.updateContent(commentUpdateDto.getContent());

        commentRepository.save(comment);
        return comment.getPost().getId();
    }

    public Long commentDelete(String loginId,Long commentId){
        Long userId = userClient.getUserIdByLoginId(loginId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new EntityNotFoundException("댓글이 없습니다."));
        Long commentWriterId = comment.getUserId();

        if (!(userId.equals(commentWriterId))){return 0L;}
        comment.delete();
        commentRepository.save(comment);
        return comment.getId();
    }

    public Long replyCreate(String loginId,ReplyCommentCreateReqDto dto){
        Long userId = userClient.getUserIdByLoginId(loginId);
        Comment parent = commentRepository.findById(dto.getParentId()).orElseThrow(()->new EntityNotFoundException("댓글이 없다"));

        // 부모 댓글이 Post용인지 Vote용인지 구분 필요 (예: parent.getPost() or parent.getVote() 확인)
        if (parent.getPost() != null) { // 일반 게시물 댓글
            Post post = commentRepository.findPostByParentId(parent.getId());

            Comment comment = Comment.builder()
                    .userId(userId)
                    .post(post)
                    .parent(parent)
                    .content(dto.getContent())
                    .build();
            commentRepository.save(comment);
            parent.addChild(comment);
            return post.getId();
        } else if (parent.getVote() != null) { // 투표 게시물 댓글
            Vote vote = commentRepository.findVoteByParentId(parent.getId());

            Comment comment = Comment.builder()
                    .userId(userId)
                    .vote(vote)
                    .parent(parent)
                    .content(dto.getContent())
                    .build();
            commentRepository.save(comment);
            parent.addChild(comment);
            return vote.getVoteId();
        } else {
            throw new IllegalStateException("부모 댓글이 어떤 게시물에도 속해있지 않습니다.");
        }
    }


    public CommentLikeResDto commentLikeToggle(Long commentId,String loginId){
        String redisKey = "comment:like" + commentId;

        Object cachedValue = commentLikeRedisTemplate.opsForValue().get(redisKey);
        Long likeCount = cachedValue != null ? ((Number) cachedValue).longValue() : null;

        Long userId = userClient.getUserIdByLoginId(loginId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new EntityNotFoundException("댓글이 없습니다."));

        if (likeCount==null){
            likeCount = commentLikeRepository.countByComment(comment);
            commentLikeRedisTemplate.opsForValue().set(redisKey,likeCount,10, TimeUnit.MINUTES);
        }

        Optional<CommentLike> commentLikeOptional = commentLikeRepository.findByCommentAndUserId(comment,userId);
        boolean isLike;

        if (commentLikeOptional.isPresent()){
            commentLikeRepository.delete(commentLikeOptional.get());
            isLike = false;
            rabbitTemplate.convertAndSend(BACKUP_QUEUE_COMMENT_ML,commentId);
            likeCount--;
        }
        else {
            CommentLike newLike = CommentLike.builder()
                    .comment(comment)
                    .userId(userId)
                    .build();
            commentLikeRepository.save(newLike);
            isLike = true;
            rabbitTemplate.convertAndSend(BACKUP_QUEUE_COMMENT_AL,commentId);
            likeCount++;
        }
        commentLikeRedisTemplate.opsForValue().set(redisKey, likeCount, 10, TimeUnit.MINUTES);

        return new CommentLikeResDto(likeCount, isLike);
    }


}
