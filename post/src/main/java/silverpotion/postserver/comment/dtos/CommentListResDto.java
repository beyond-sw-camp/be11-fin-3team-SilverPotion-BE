package silverpotion.postserver.comment.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.postserver.comment.domain.Comment;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.post.dtos.UserProfileInfoDto;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommentListResDto {
    private Long commentId;
    private Long userId;
    private Long parentId;
    private String nickName;
    private String profileImage;
    @JsonInclude(JsonInclude.Include.NON_NULL) //null이면 생략
    private List<CommentListResDto> replies;//대댓글 리스트
    private String content;
    private Long likeCount;
    private String isUpdate;
    private DelYN delYn;
    private String isLike;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdTime;

    public static CommentListResDto fromEntity(Comment comment, Long likeCount, String isLike, UserProfileInfoDto userProfileInfoDto) {
        return CommentListResDto.builder()
                .commentId(comment.getId())
                .userId(comment.getUserId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .nickName(userProfileInfoDto.getNickname())
                .profileImage(userProfileInfoDto.getProfileImage())
                .content(comment.getContent())
                .likeCount(likeCount)
                .isUpdate(determineUpdateStatus(comment))
                .delYn(comment.getDelYn())
                .isLike(isLike)
                .createdTime((comment.getCreatedTime()))
                .build();
    }


    public void setReplies(List<CommentListResDto> replies) {
        this.replies = replies;
    }

    // 수정 여부 판단
    private static String determineUpdateStatus(Comment comment) {
        return (comment.getUpdatedTime() != null && !comment.getUpdatedTime().equals(comment.getCreatedTime())) ? "Y" : "N";
    }
}
