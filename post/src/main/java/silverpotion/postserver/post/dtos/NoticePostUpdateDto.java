package silverpotion.postserver.post.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoticePostUpdateDto implements PostUpdateDto {
    private String title;
    private String content;
    private List<MultipartFile> postImg;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getContent() {
        return content;
    }
}
