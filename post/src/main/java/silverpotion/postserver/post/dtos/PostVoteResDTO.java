package silverpotion.postserver.post.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.postserver.post.domain.Post;
import silverpotion.postserver.post.domain.PostCategory;
import silverpotion.postserver.post.domain.PostFile;
import silverpotion.postserver.post.domain.VoteOptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostVoteResDTO {
    private Long id; // post면 post.id, vote면 vote.voteid
    private String title;
    private String content;
    private Long writerId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private String nickname;
    private String profileImage;
    private List<String> imageUrls;
    private PostCategory postCategory;
    private Long likeCount;
    private Long commentCount;

    private Integer viewCount;       // POST 전용
    private Boolean multipleChoice;  // VOTE 전용
    private List<String> voteOptions;// VOTE 전용
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closeTime;
    private boolean participating;
    private String postType;// post/vote

    public void setVoteOptions(List<VoteOptions> options){
        this.voteOptions = options.stream().map(VoteOptions::getOptionText).collect(Collectors.toList());
    }
}
