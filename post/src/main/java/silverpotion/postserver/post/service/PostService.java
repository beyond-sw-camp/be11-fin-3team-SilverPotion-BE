package silverpotion.postserver.post.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import silverpotion.postserver.gathering.domain.Gathering;
import silverpotion.postserver.gathering.repository.gatheringRepository;
import silverpotion.postserver.post.domain.Post;
import silverpotion.postserver.post.domain.PostFile;
import silverpotion.postserver.post.domain.PostStatus;
import silverpotion.postserver.post.dtos.PostInitDto;
import silverpotion.postserver.post.dtos.FreePostUpdateDto;
import silverpotion.postserver.post.dtos.PostUpdateDto;
import silverpotion.postserver.post.dtos.VotePostUpdateDto;
import silverpotion.postserver.post.repository.PostFileRepository;
import silverpotion.postserver.post.repository.PostRepository;
import silverpotion.postserver.post.userClient.UserClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;


@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final gatheringRepository gatheringRepository;
    private final PostFileRepository postFileRepository;
    private final S3Client s3Client;
    private final UserClient userClient;
//    private final NotificationService notificationService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;

    public PostService(PostRepository postRepository, silverpotion.postserver.gathering.repository.gatheringRepository gatheringRepository, PostFileRepository postFileRepository, S3Client s3Client, UserClient userClient) {
        this.postRepository = postRepository;
        this.gatheringRepository = gatheringRepository;
        this.postFileRepository = postFileRepository;
        this.s3Client = s3Client;
        this.userClient = userClient;
    }

//    1. 게시물 생성시, 카테고리 유형 저장(임시저장)
    public Long createDraftPost(PostInitDto dto){
        Gathering gathering = gatheringRepository.findById(dto.getGatheringId()).orElseThrow(()-> new EntityNotFoundException("gathering is not found"));

        Post draftPost = Post.builder()
                .gathering(gathering)
                .postCategory(dto.getPostCategory())
                .postStatus(PostStatus.draft)
                .viewCount(0)
                .build();
        return postRepository.save(draftPost).getId();

    }
//  2. 게시물 최종 저장(카테고리 분기)
    public Object updateFinalPost(Long postId, String loginId, PostUpdateDto dto){
        Long userId = userClient.getUserIdByLoginId(loginId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물 없음"));

        switch (post.getPostCategory()) {
            case free:
                FreePostUpdateDto freeDto = (FreePostUpdateDto) dto;
                saveCommonPost(post, userId, (FreePostUpdateDto) dto);
                return freeDto;

//            case notice:
//                NoticePostUpdateDto noticeDto = (NoticePostUpdateDto) dto;
//                saveCommonPost(post, userId, (NoticePostUpdateDto) noticeDto);
//                notificationService.sendToAllMembers(
//                        "새로운 공지사항", dto.getTitle(), "/post/" + post.getId()
//                );
//                return noticeDto;

            case vote:
                VotePostUpdateDto voteDto = (VotePostUpdateDto) dto;
                saveVotePost(post, userId, (VotePostUpdateDto) voteDto);
                return voteDto;
            default:
                throw new UnsupportedOperationException("지원하지 않는 게시물 유형");
        }

    }

    // 3. 공통 저장 로직 (자유글/공지글 공통)
    private void saveCommonPost(Post post, Long userId, FreePostUpdateDto dto) {
        post.update(dto.getTitle(), dto.getContent());
        post.assignWriter(userId);
        postRepository.save(post);

        for (MultipartFile file : dto.getPostImg()) {
            String fileUrl = uploadImage(file);
            postFileRepository.save(new PostFile(post, fileUrl));
        }
    }

    // 4. 투표 게시물 저장
    private void saveVotePost(Post post, Long userId, VotePostUpdateDto dto) {
        post.update(dto.getTitle(), dto.getDescription()); // 예시
        post.assignWriter(userId);
        postRepository.save(post);
        // 투표 항목 저장 등 로직 추가 필요
    }

//  5. S3에 이미지저장
    public String uploadImage(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

//    게시물 삭제
    public void delete(Long postId,String loginId){
        Long userId = userClient.getUserIdByLoginId(loginId);
        Post post = postRepository.findById(postId).orElseThrow(()->new EntityNotFoundException("게시물을 찾을 수 없습니다."));
        if (!userId.equals(post.getId())){return;}
        postRepository.delete(post);
    }


}
