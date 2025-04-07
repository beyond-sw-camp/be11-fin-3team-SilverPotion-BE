package silverpotion.userserver.openAi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import silverpotion.userserver.common.dto.CommonDto;
import silverpotion.userserver.openAi.dto.HealtReportOfDepReqDto;
import silverpotion.userserver.openAi.dto.HealthReportDto;
import silverpotion.userserver.openAi.service.HealthReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("silverpotion/gptchat")
public class HealthReportController {
    private final HealthReportService healthReportService;

    public HealthReportController(HealthReportService healthReportService) {
        this.healthReportService = healthReportService;
    }
    //1. 헬스리포트 생성 및 실시간 조회
    @GetMapping("/gptqna")
    public Mono<ResponseEntity<?>> reportCreate(@RequestHeader("X-User-LoginId")String loginId) {
//mono를 리턴하는 웹플럭스 방식이기 때문에 아래처럼 리턴한다. .map은 mono안에 있는 진짜 값을 뽑아냄.(여기선 Mono<String>이니까 String타입의 내용을 꺼내서 우리가 원하는ResponseEntity 방식으로 리턴!)
        return healthReportService.chatWithGpt(loginId).map(content->ResponseEntity.status(HttpStatus.CREATED).body(new CommonDto(HttpStatus.CREATED.value(), "success",content)));
    }

    //2. 지난 헬스리포트 조회(selecetedDate부분에 yyyy-mm-dd 형식으로 받아와야함)
    @GetMapping("/pastReport/{selectedDate}")
    public ResponseEntity<?> pastReport(@RequestHeader("X-User-LoginId")String loginId, @PathVariable String selectedDate){
       HealthReportDto dto = healthReportService.pastReport(loginId,selectedDate);
       return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"healthReport you select is uploaded successfully",dto),HttpStatus.OK);
    }

    //3. 내 피보호자 헬스리포트 조회
    @GetMapping("/pastReportOfDep")
    public ResponseEntity<?> pastReportOfDep(@RequestHeader("X-User-LoginId")String loginId, @RequestBody HealtReportOfDepReqDto reqDto){
        HealthReportDto dto = healthReportService.pastReportOfDep(loginId,reqDto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"healthReport of your dependent is uploaded successfully",dto),HttpStatus.OK);


    }



}
