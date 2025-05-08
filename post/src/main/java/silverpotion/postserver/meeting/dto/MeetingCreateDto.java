package silverpotion.postserver.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MeetingCreateDto {
    private Long gatheringId;
    private String name;
    private LocalDate meetingDate;
    private LocalTime meetingTime;
    private String place;
    private MultipartFile imageFile;
    private Long cost;
    private Long maxPeople;
    private Double lat;
    private Double lon;
}
