package silverpotion.postserver.gathering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GatheringPeopleDto {
    private Long gatheringPeopleId;
    private Long gatheringId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String introduce;
    private String status;
    private LocalDateTime createdTime;
    private String loginId;
}
