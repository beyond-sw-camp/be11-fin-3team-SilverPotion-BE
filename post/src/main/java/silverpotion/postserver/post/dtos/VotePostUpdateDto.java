package silverpotion.postserver.post.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VotePostUpdateDto implements PostUpdateDto{
    private String title;
    private String description; // 투표 내용
    private List<String> voteOptions; //투표항목
    private boolean multipleChoice; //복수선택 여부

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getContent() {
        return description; //getContent는 description을 반환
    }
}
