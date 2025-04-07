package silverpotion.userserver.healthData.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import silverpotion.userserver.common.dto.CommonDto;
import silverpotion.userserver.healthData.dtos.*;
import silverpotion.userserver.healthData.service.HealthDataService;

@RestController
@RequestMapping("/silverpotion/health")
public class HealthDataController {

    private final HealthDataService healthDataService;


    public HealthDataController(HealthDataService healthDataService) {
        this.healthDataService = healthDataService;
    }

    //    1.앱으로부터 데이터 받아오는 api
    @PostMapping("/fromPhone")
    public void receiveData(@RequestBody HealthSyncDto dto, @RequestHeader("X-User-LoginId") String loginId) {
        healthDataService.save(dto, loginId);
    }


    //    2.헬스데이터 오늘꺼 조회
    @GetMapping("/dataList")
    public ResponseEntity<?> myHealthData(@RequestHeader("X-User-LoginId") String loginId) {
        HealthDataListDto todayData = healthDataService.todayData(loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "today's healthData is uploaded well", todayData), HttpStatus.OK);
    }

    //    3.헬스데이터 특정날짜 조회
    @PostMapping("/specificDataList")
    public ResponseEntity<?> mySpecificHealthData(@RequestBody HealthDataSpecificDateDto dto, @RequestHeader("X-User-LoginId") String loginId) {
        HealthDataListDto specificData = healthDataService.specificDateData(dto, loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "today's healthData is uploaded well", specificData), HttpStatus.OK);
    }

    //    4.헬스데이터 지난주 평균 조회(지난주)
    @GetMapping("/weeklyavg")
    public ResponseEntity<?> weeklyAvgHealthData(@RequestHeader("X-User-LoginId") String loginId) {
        HealthAvgDataDto weeklyAvg = healthDataService.weeklyAvgHealthData(loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "weekly AvgHealthData is uploaded well", weeklyAvg), HttpStatus.OK);
    }

    //   5.헬스데이터 이번달 평균 조회
    @GetMapping("/monthlyavg")
    public ResponseEntity<?> monthlyAvgHealthData(@RequestHeader("X-User-LoginId") String loginId) {
        HealthAvgDataDto monthlyAvg = healthDataService.monthlyAvgHealthData(loginId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "monthly AvgHealthData is uploaded well", monthlyAvg), HttpStatus.OK);
    }

    //    6.내 피보호자 현재 헬스데이터 조회
    @GetMapping("/yourHealthData/{id}")
    public ResponseEntity<?> mydependentData(@RequestHeader("X-User-LoginId") String loginId, @PathVariable Long id) {
        HealthDataListDto depentData = healthDataService.mydependentData(loginId, id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "Health Data Of my Dependent is uploaded successfully", depentData), HttpStatus.OK);
    }

    //   7. 특정 주 평균헬스데이터 조회
    @GetMapping("/selectWeek")
    public ResponseEntity<?> mySpecificWeekHealthData(@RequestHeader("X-User-LoginId")String loginId, @RequestBody SelectDateReqDto dto){
        HealthDataListDto mySpecificHealthData = healthDataService.mySpecificWeekHealthData(loginId,dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"weekly AvgHealthData you selcect is uploaded successfully",mySpecificHealthData),HttpStatus.OK);
    }

    //  8. 특정 월 평균 헬스데이터 조회
    @GetMapping("/selectMonth")
    public ResponseEntity<?> mySpecificMonthHealthData(@RequestHeader("X-User-LoginId")String loginId, @RequestBody SelectDateReqDto dto){
        HealthDataListDto mySpecificMonthHealthData = healthDataService.mySpecificMonthHealthData(loginId,dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"monthly AvgHealthData you selcect is uploaded successfully",mySpecificMonthHealthData),HttpStatus.OK);
    }

    // 9. 내 피보호자 특정 주 평균헬스데이터 조회
    @GetMapping("/dependent/week")
    public ResponseEntity<?> myDependentWeekHealthData(@RequestHeader("X-User-LoginId")String loginId, @RequestBody SelectDateAndDepReqDto dto){
        HealthDataListDto dependentWeekHealthData = healthDataService.myDependentWeekHealthData(loginId,dto);
        return new ResponseEntity<>((new CommonDto(HttpStatus.OK.value(), "My dependent's weekly AvgHealthData you select is uploaded successfully",dependentWeekHealthData)),HttpStatus.OK);
    }

    //10. 내 피보호자 특정 월 평균 헬스데이터 조회
    @GetMapping("/dependent/month")
    public ResponseEntity<?> myDependentMonthHealthData(@RequestHeader("X-User-LoginId")String loginId, @RequestBody SelectDateAndDepReqDto dto){
        HealthDataListDto dependentMonthHealthData = healthDataService.myDependentMonthHealthData(loginId,dto);
        return new ResponseEntity<>((new CommonDto(HttpStatus.OK.value(), "My dependent's weekly AvgHealthData you select is uploaded successfully",dependentMonthHealthData)),HttpStatus.OK);
    }



}










