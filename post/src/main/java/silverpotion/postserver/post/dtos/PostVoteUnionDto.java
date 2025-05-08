package silverpotion.postserver.post.dtos;

import silverpotion.postserver.post.domain.PostCategory;
import silverpotion.postserver.post.domain.PostFile;

import java.time.LocalDateTime;
import java.util.List;

public interface PostVoteUnionDto {
    Long getId(); //post.id or vote.voteId
    String getTitle();
    String getContent();//post.content or vote.description
    LocalDateTime getCreatedAt();
    String getPostType(); //post or vote
    String getNickname();
    String getProfileImage();
    PostCategory getPostCategory();

    //선택적  필드
    Integer getViewCount(); //post에서만 존재
    Boolean getMultiChoice();//vote에서만 존재
    List<String> getVoteOptions();//vote에서만 존재
}
