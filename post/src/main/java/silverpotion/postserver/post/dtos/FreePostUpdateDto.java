package silverpotion.postserver.post.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import silverpotion.postserver.post.domain.Post;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FreePostUpdateDto implements PostUpdateDto{
    private List<MultipartFile> postImg;
    private String title;
    private String content;

    public Post savePost(Long userId){
        return Post.builder()
                .writerId(userId)
                .title(this.title)
                .content(this.content)
                .build();
    }
    @Override
    public String getTitle(){
        return title;
    }
    @Override
    public String getContent(){
        return title;
    }



}
