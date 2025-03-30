package silverpotion.postserver.post.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import silverpotion.postserver.post.domain.PostCategory;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostInitDto {
    private PostCategory postCategory;
    private Long gatheringId;
}
