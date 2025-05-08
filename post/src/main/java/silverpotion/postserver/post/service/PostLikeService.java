package silverpotion.postserver.post.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import silverpotion.postserver.notification.dto.NotificationMessageDto;
import silverpotion.postserver.notification.service.NotificationProducer;
import silverpotion.postserver.post.feignClient.UserClient;
import silverpotion.postserver.post.domain.Post;
import silverpotion.postserver.post.domain.PostLike;
import silverpotion.postserver.post.domain.Vote;
import silverpotion.postserver.post.domain.VoteLike;
import silverpotion.postserver.post.dtos.PostLikeResDto;
import silverpotion.postserver.post.dtos.VoteResListDto;
import silverpotion.postserver.post.repository.PostLikeRepository;
import silverpotion.postserver.post.repository.PostRepository;
import silverpotion.postserver.post.repository.VoteLikeRepository;
import silverpotion.postserver.post.repository.VoteRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static silverpotion.postserver.common.config.RabbitMQConfig.BACKUP_QUEUE_ML;
import static silverpotion.postserver.common.config.RabbitMQConfig.BACKUP_QUEUE_AL;


@Service
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;
    private final VoteRepository voteRepository;
    private final VoteLikeRepository voteLikeRepository;
    private final NotificationProducer notificationProducer;

    @Qualifier("postLikeRedisTemple")
    private final RedisTemplate<String,Object> postLikeRedisTemplate;

    @Qualifier("voteLikeRedisTemple")
    private final RedisTemplate<String,Object> voteLikeRedisTemplate;


    public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository, UserClient userClient, RabbitTemplate rabbitTemplate, VoteRepository voteRepository, VoteLikeRepository voteLikeRepository, NotificationProducer notificationProducer, @Qualifier("postLikeRedisTemple") RedisTemplate<String, Object> postLikeRedisTemplate, @Qualifier("voteLikeRedisTemple") RedisTemplate<String, Object> voteLikeRedisTemplate) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.userClient = userClient;
        this.rabbitTemplate = rabbitTemplate;
        this.voteRepository = voteRepository;
        this.voteLikeRepository = voteLikeRepository;
        this.notificationProducer = notificationProducer;
        this.postLikeRedisTemplate = postLikeRedisTemplate;
        this.voteLikeRedisTemplate = voteLikeRedisTemplate;
    }

    @Transactional
    public PostLikeResDto togglePostLike(Long postId,String loginId){
        //레디스 좋아요 키
        String redisKey = "post:like:" + postId;

//        캐싱값
        Object cachedValue = postLikeRedisTemplate.opsForValue().get(redisKey);
        Long likeCount = cachedValue != null ? ((Number) cachedValue).longValue() : null;

        Long userId = userClient.getUserIdByLoginId(loginId);
        Post post = postRepository.findById(postId).orElseThrow(()->new EntityNotFoundException("해당 게시물이 존재하지 않습니다."));

        if (likeCount == null) {
            likeCount = postLikeRepository.countByPost(post);
            postLikeRedisTemplate.opsForValue().set(redisKey, likeCount, 10, TimeUnit.MINUTES);
        }

        Optional<PostLike> postLikeOpt = postLikeRepository.findByPostAndUserId(post,userId);
        boolean isLike;

        if (postLikeOpt.isPresent()){
            postLikeRepository.delete(postLikeOpt.get());
            isLike = false;
            rabbitTemplate.convertAndSend(BACKUP_QUEUE_ML, postId);
            likeCount--;
        } else {
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .userId(userId)
                    .build();
            postLikeRepository.save(newLike);
            isLike = true;
            rabbitTemplate.convertAndSend(BACKUP_QUEUE_AL, postId);
            likeCount++;
        }
        postLikeRedisTemplate.opsForValue().set(redisKey, likeCount,10, TimeUnit.MINUTES);

        return new PostLikeResDto(likeCount,isLike);
    }

//    투표좋아요 토클
    @Transactional
    public PostLikeResDto toggleVoteLike(Long voteId, String loginId){
        //레디스 좋아요 키
        String redisKey = "vote:like:" + voteId;

//        캐싱값
        Object cachedValue = voteLikeRedisTemplate.opsForValue().get(redisKey);
        Long voteLikeCount = cachedValue != null ? ((Number) cachedValue).longValue() : null;

        Long userId = userClient.getUserIdByLoginId(loginId);
        Vote vote = voteRepository.findById(voteId).orElseThrow(()->new EntityNotFoundException("해당 게시물이 존재하지 않습니다."));

        if (voteLikeCount == null) {
            voteLikeCount = voteLikeRepository.countByVote(vote);
            voteLikeRedisTemplate.opsForValue().set(redisKey,voteLikeCount,10,TimeUnit.MINUTES);
        }

        Optional<VoteLike> voteLikeOpt = voteLikeRepository.findByVoteAndUserId(vote,userId);
        boolean isLike;

        if (voteLikeOpt.isPresent()){
            voteLikeRepository.delete(voteLikeOpt.get());
            isLike = false;
            rabbitTemplate.convertAndSend(BACKUP_QUEUE_ML, voteId);
            voteLikeCount--;
        } else {
            VoteLike newLike = VoteLike.builder()
                    .vote(vote)
                    .userId(userId)
                    .build();
            voteLikeRepository.save(newLike);
            isLike = true;
            rabbitTemplate.convertAndSend(BACKUP_QUEUE_AL, voteId);
            voteLikeCount++;

            // 좋아요 알림 전송
            String voteNickName = userClient.getNicknameByUserId(userId);
            Long writerId = vote.getWriterId(); // 게시물 작성자 ID (필드 이름에 따라 다를 수 있음)
            if (!userId.equals(writerId)) { // 자기 자신이 좋아요 누른 경우 제외
                String writerLoginId = userClient.getLoginIdByUserId(writerId);

                NotificationMessageDto notification = NotificationMessageDto.builder()
                        .loginId(writerLoginId)
                        .title("좋아요 알림")
                        .content("'" + voteNickName + "'님이 회원님의 게시글에 좋아요를 눌렀습니다.")
                        .type("VOTE_LIKED")
                        .referenceId(voteId)
                        .build();

                notificationProducer.sendNotification(notification);
            }
        }
        vote.changeVoteLike(voteLikeCount);
        voteRepository.save(vote);
        voteLikeRedisTemplate.opsForValue().set(redisKey, voteLikeCount,10, TimeUnit.MINUTES);

        return new PostLikeResDto(voteLikeCount,isLike);
    }

}
