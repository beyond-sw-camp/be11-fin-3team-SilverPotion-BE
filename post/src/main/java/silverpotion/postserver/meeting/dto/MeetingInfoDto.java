package silverpotion.postserver.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MeetingInfoDto {

    private Long meetingId;
    private Long gatheringId;
    private String name;
    private LocalDate meetingDate;
    private LocalTime meetingTime;
    private String place;
    private String imageUrl;
    private Long cost;
    private Long maxPeople;
    private Double lat;
    private Double lon;
    // 참석자 리스트 추가
    private List<AttendeeDto> attendees;
}
