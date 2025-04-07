package silverpotion.userserver.report.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.userserver.common.dto.CommonDto;
import silverpotion.userserver.report.domain.Report;
import silverpotion.userserver.report.dtos.*;
import silverpotion.userserver.report.service.ReportService;


@RestController
@RequestMapping("/silverpotion/report")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> reportCreate(@RequestBody ReportCreateResDto reportCreateResDto
            , @RequestHeader("X-User-LoginId") String loginId){
        Report report = reportService.reportCreate(reportCreateResDto,loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(),"신고 등록 완료",report),HttpStatus.CREATED);
    }

//    신고 목록 조회
    @GetMapping("/list")
//    @PreAuthorize("hasRole('ADMIN))
    public ResponseEntity<?> getReportList(@RequestHeader("X-User-LoginId") String loginId, Pageable pageable, ReportRequestDto reportRequestDto){
        Page<ReportResponseDto> reports = reportService.findAllReports(loginId, pageable,reportRequestDto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"신고 유저 조회 성공",reports),HttpStatus.OK);
    }

//    신고 상세 조회
    @GetMapping("/detail/{reportId}")
//    @PreAuthorize("hasRole('ADMIN))
    public ResponseEntity<?> getReportDetails(@PathVariable Long reportId){
        ReportDetailListDto dto = reportService.getReportDetails(reportId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "detail is uploaded successfully",dto),HttpStatus.OK);
    }

//    특정 신고 처리
    @PostMapping("{reportId}/process")
//    @PreAuthorize("hasRole('ADMIN))
    public ResponseEntity<?> getReportProcess(@PathVariable Long reportId, @RequestBody @Valid ReportProcessResDto dto){
        ReportProcessResDto reportProcessResDto = reportService.processReport(reportId,dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "process is uploaded successfully",reportProcessResDto),HttpStatus.OK);
    }
}
