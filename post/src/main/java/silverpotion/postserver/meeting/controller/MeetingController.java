package silverpotion.postserver.meeting.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.postserver.common.dto.CommonDto;
import silverpotion.postserver.gathering.dto.GatheringInfoDto;
import silverpotion.postserver.meeting.dto.MeetingAttendDto;
import silverpotion.postserver.meeting.dto.MeetingCreateDto;
import silverpotion.postserver.meeting.dto.MeetingInfoDto;
import silverpotion.postserver.meeting.dto.MeetingUpdateDto;
import silverpotion.postserver.meeting.service.MeetingService;

import java.util.List;

@RestController
@RequestMapping("silverpotion/meeting")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    // 정모 생성
    @PostMapping("/create")
    public ResponseEntity<?> createMeeting(
            @RequestHeader("X-User-LoginId") String loginId,
            @ModelAttribute MeetingCreateDto dto) {

        meetingService.createMeeting(loginId, dto);
        String name = dto.getName();
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "정모가 성공적으로 생성되었습니다.", name), HttpStatus.OK);
    }

    // 정모 수정
    @PatchMapping("/update/{meetingId}")
    public ResponseEntity<?> updateMeeting(
            @RequestHeader("X-User-LoginId") String loginId,
            @PathVariable Long meetingId,
            @ModelAttribute MeetingUpdateDto dto) {

        meetingService.updateMeeting(loginId, meetingId, dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "정모가 수정되었습니다.", meetingId), HttpStatus.OK);
    }

    // 모임별 정모 조회
    @GetMapping("/{gatheringId}/list")
    public ResponseEntity<?> getMeetingsByGatheringId(@PathVariable Long gatheringId) {
        List<MeetingInfoDto>dtos = meetingService.getMeetingsByGatheringId(gatheringId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "해당 모임의 정모가 조회되었습니다.", dtos), HttpStatus.OK);
    }

    // 정모 참석
    @PostMapping("/attend")
    public ResponseEntity<?> attendMeeting(
            @RequestHeader("X-User-LoginId") String loginId,
            @RequestBody MeetingAttendDto dto) {

        meetingService.attendMeeting(loginId, dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "정모에 참석되었습니다.", dto), HttpStatus.OK);
    }

    // 정모 참석 취소
    @DeleteMapping("/deleteattend")
    public ResponseEntity<?> cancelAttendance(
            @RequestHeader("X-User-LoginId") String loginId,
            @RequestBody MeetingAttendDto dto) {

        meetingService.cancelAttendance(loginId, dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "정모 참석이 취소되었습니다.", dto), HttpStatus.OK);
    }

    // 정모 상세 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<?> getMeetingById(@PathVariable Long meetingId) {
        MeetingInfoDto meetingInfoDto = meetingService.getMeetingById(meetingId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "해당 모임의 상세정보가 조회되었습니다.", meetingInfoDto), HttpStatus.OK);
    }

    // 다가오는 정모 조회
    @GetMapping("/upcoming")
    public ResponseEntity<?> getMeetingsWithinAWeek() {
        List<MeetingInfoDto> dtos = meetingService.getMeetingsWithinAWeek();
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "이번 주 예정된 정모들이 조회되었습니다.", dtos), HttpStatus.OK);
    }

    // 정모 삭제
    @PatchMapping("/delete/{meetingId}")
    public ResponseEntity<?> deleteMeeting(
            @PathVariable Long meetingId,
            @RequestHeader("X-User-LoginId") String loginId) {
        meetingService.deleteMeeting(meetingId, loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "정모가 삭제되었습니다.", meetingId), HttpStatus.OK);
    }

    // 주변 정모 검색
    @GetMapping("/nearby")
    public ResponseEntity<List<MeetingInfoDto>> getNearbyMeetings(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "2") double radius) {
        List<MeetingInfoDto> nearbyMeetings = meetingService.findNearbyMeetings(lat, lon, radius);
        return ResponseEntity.ok(nearbyMeetings);
    }

    // 내 정모 조회
    @GetMapping("/mymeetings")
    public ResponseEntity<?> getMyMeetings(@RequestHeader("X-User-LoginId") String loginId) {
        List<MeetingInfoDto> dtos = meetingService.getMyMeetings(loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "내가 속한 정모들이 조회되었습니다.", dtos), HttpStatus.OK);
    }
}
