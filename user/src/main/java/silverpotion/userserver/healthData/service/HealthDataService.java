package silverpotion.userserver.healthData.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import silverpotion.userserver.careRelation.domain.CareRelation;
import silverpotion.userserver.careRelation.domain.LinkStatus;
import silverpotion.userserver.fireBase.service.FireBaseService;
import silverpotion.userserver.healthData.domain.DataType;
import silverpotion.userserver.healthData.domain.HealthData;
import silverpotion.userserver.healthData.domain.HeartRateData;
import silverpotion.userserver.healthData.dtos.*;
import silverpotion.userserver.healthData.reopisitory.HealthDataRepository;
import silverpotion.userserver.user.domain.DelYN;
import silverpotion.userserver.user.domain.User;
import silverpotion.userserver.user.repository.UserRepository;
import silverpotion.userserver.userDetailHealthInfo.domain.UserDetailHealthInfo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HealthDataService {
    private final HealthDataRepository healthDataRepository;
    private final UserRepository userRepository;
    private final FireBaseService fireBaseService;


    public HealthDataService(HealthDataRepository healthDataRepository, UserRepository userRepository, FireBaseService fireBaseService) {
        this.healthDataRepository = healthDataRepository;
        this.userRepository = userRepository;
        this.fireBaseService = fireBaseService;
    }

    //  0.앱으로부터 데이터를 받아와 HealthData 생성
    public void save(HealthSyncDto dto,String loinId){
        User user = userRepository.findByLoginIdAndDelYN(loinId, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        //오늘 날짜
        LocalDate today =LocalDate.now();


        //평균 심박수
        List<HeartRateData>beatList = dto.getHeartRateData();
        int averageBpm =0;
        if(beatList != null && ! beatList.isEmpty()){
            int sum = 0;
            for(HeartRateData h : beatList){
                sum +=(int)h.getBpm();
                averageBpm = sum / beatList.size();
            }
        }

        //엔티티 객체 생성
        Optional<HealthData> todayHealthData = healthDataRepository.findByUserIdAndCreatedDateAndDataType(user.getId(), today, DataType.DAY);
        if(todayHealthData.isPresent()){ // 이미 오늘 날짜에 생성된 헬스데이터가 있다면 기존의 것을 지우고 최근 엔티티 객체를 새로 저장
          todayHealthData.get().update(dto,averageBpm);
        } else{ // 오늘날짜에 생성된 헬스데이터가 없다면 바로 저장
          DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
          String period = today.format(formatter);
          HealthData data = healthDataRepository.save(dto.toEntityFromSync(averageBpm,user,today,period));
          user.getMyHealthData().add(data);

        }
    }

    //  1. 사용자의 앱에 헬스데이터 보내달라고 요청하는 api
    public void sendHealthDataReq(String loginId){
        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        fireBaseService.sendHealthSyncReq(user.getFireBaseToken()); //유저의 파이어베이스 토큰을 매개로 유저의 디바이스에 헬스데이터보내달라고 알림을 보냄
    }

//    //  2. 헬스데이터 오늘꺼 조회
//    public HealthDataListDto todayData(String loginId){
//        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//        LocalDate today = LocalDate.now();
//        return healthDataRepository.findByUserIdAndCreatedDateAndDataType(user.getId(),today,DataType.DAY).orElseThrow(()->new EntityNotFoundException("금일 기록이 없습니다")).toListDtoFromEntity(user.getProfileImage());
//    }

//    //    3. 헬스데이터 특정날짜 조회
//    public HealthDataListDto specificDateData(HealthDataSpecificDateDto dto, String loginId){
//        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//        LocalDate specificDate = LocalDate.parse(dto.getSpecificDate());
//        return healthDataRepository.findByUserIdAndCreatedDateAndDataType(user.getId(), specificDate,DataType.DAY).orElseThrow(()->new EntityNotFoundException("해당날짜의 기록이 없습니다")).toListDtoFromEntity(user.getProfileImage());
//    }

    //   4.헬스데이터 지난주 평균 조회
    public HealthAvgDataDto weeklyAvgHealthData(String loginId){
        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(DayOfWeek.MONDAY);//이번주 월요일
        //내 헬스데이터중 주간평균타입으로 필터를 거르고 그중 생성날짜가 이번주 월요일에 생성된걸로 픽하면 전주 일주일치 평균데이터임
        HealthData weekData = user.getMyHealthData().stream().filter(w->w.getDataType()==DataType.WEEKAVG)
                                    .filter(w->w.getCreatedDate().equals(thisMonday)).findFirst().orElseThrow(()->new EntityNotFoundException("주간 데이터가 없습니다"));
        return weekData.toAvgDtoFromEntity();

    }

    //   5.헬스데이터 월별 평균 조회(이번달)
    public HealthAvgDataDto monthlyAvgHealthData(String loginId) {
        User user = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다"));
        LocalDate today = LocalDate.now(); //오늘 날짜
        LocalDate firstDayOfMonth = today.withDayOfMonth(1); //이번달 시작 날짜
        HealthData monthData = user.getMyHealthData().stream().filter(w->w.getDataType()==DataType.MONTHAVG)
                                .filter(w->w.getCreatedDate().equals(firstDayOfMonth)).findFirst().orElseThrow(()->new EntityNotFoundException("월간 데이터가 없습니다"));

       return monthData.toAvgDtoFromEntity();
    }

////    6.나의 피보호자 헬스데이터 조회(피보호자 목록 탭 조회할 때 백엔드로부터 피보호자 id를 받게되므로 프론트는 그 id를 가지고 피보호자 데이터 조회하도록 설계)
//     public HealthDataListDto mydependentData(String loginId, Long id){
//        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()-> new EntityNotFoundException("없는 회원입니다"));
//        User dependentUser = userRepository.findByIdAndDelYN(id,DelYN.N).orElseThrow(()-> new EntityNotFoundException("없는 회원입니다"));
//        //careRelation 객체가 생성되면 상태 기본값이 pending이긴해도 user와 양방향 매핑으로 인해 자동으로 칼럼 리스트에 add되기 때문에 pending인거 제외하고 connect인거만 가져오는 것
//        List<CareRelation> relationAsProtector = user.getAsProtectors().stream().filter(c->c.getLinkStatus() == LinkStatus.CONNECTED).toList();
//         List<HealthData> dependentDataList = new ArrayList<>();
//        boolean isThereDependent = false; //요청으로 들어온 피보호자가 진짜 피보호자인지 구분하는 불린
//        for(CareRelation c: relationAsProtector){
//           if(c.getDependent().getId().equals(id)){ //보호자 유저 의 dependent중 요청으로 들어온 아이디값과 같다면,
//                dependentDataList= c.getDependent().getMyHealthData();
//                isThereDependent = true;
//           }
//        }
//        if(!isThereDependent){throw new IllegalArgumentException("해당 유저는 피보호자로 등록되지 않은 유저입니다");
//        }
//        LocalDate today = LocalDate.now();
//        HealthData targetData = new HealthData();
//        boolean isThereTodayData = false; // 피보호자의 오늘 데이터가 있는지 없는지 두는 불린
//        for(HealthData h :dependentDataList){
//            if(h.getCreatedDate().equals(today) && h.getDataType()==DataType.DAY){
//                targetData = h; //피보호자의 오늘 데이터
//                isThereTodayData = true;
//            } else{
//                isThereTodayData = false;
//            }
//        }
//         if(isThereTodayData){
//             return  targetData.toListDtoFromEntity(dependentUser.getProfileImage());
//         } else{
//             throw new IllegalArgumentException("피보호자의 금일 기록이 없습니다");
//         }
//     }
//
//
////   7. 특정주 평균헬스데이터 조회
//    public HealthDataListDto mySpecificWeekHealthData(String loginId, SelectDateReqDto dto){
//        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//        //항상 월요일에 주간데이터가 만들어지니까 먼저 사용자가 입력한 날짜가 속한 주의 월요일을 찾는다.(그런데 그 주 평균 데이터는 다음주 월요일에 만들어지니까 +1주)
//        LocalDate targetMonday = LocalDate.parse(dto.getSelectedDate()).with(DayOfWeek.MONDAY).plusWeeks(1);
//        //findFirst하는 이유는 리스트로 뽑히는데 어차피 그날에 만들어진 데이터는 1개일 것
//        HealthData targetData = user.getMyHealthData().stream().filter(h->h.getCreatedDate().equals(targetMonday)).filter(h->h.getDataType()==DataType.WEEKAVG)
//                                .findFirst().orElseThrow(()->new EntityNotFoundException("해당 주간 평균데이터가 존재하지 않습니다"));
//        return targetData.toListDtoFromEntity(user.getProfileImage());
//    }
//
////   8.특정 월 평균헬스데이터 조회
//    public HealthDataListDto mySpecificMonthHealthData(String loginId, SelectDateReqDto dto){
//        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//        //매달 1일에 월간데이터가 만들어지니까 먼저 사용자가 입력한 달의 +1달의 첫째날을 찾는다
//        LocalDate targetFirstDay = LocalDate.parse(dto.getSelectedDate()).plusMonths(1).withDayOfMonth(1);
//        HealthData targetData = user.getMyHealthData().stream().filter(h->h.getCreatedDate().equals(targetFirstDay)).filter(h->h.getDataType()==DataType.MONTHAVG)
//                                .findFirst().orElseThrow(()->new EntityNotFoundException("해당 월간데이터가 존재하지 않습니다"));
//        return targetData.toListDtoFromEntity(user.getProfileImage());
//    }
//
//    //    09. 내 피보호자 특정 주 평균헬스데이터 조회
//    public HealthDataListDto myDependentWeekHealthData(String loginId, SelectDateAndDepReqDto dto){
//        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//        User dependent = userRepository.findByIdAndDelYN(dto.getDependentId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//        //CareRelation객체는 유저와 양방향 매핑으로 자동으로 유저의 칼럼에 추가됨.(수락하지않은 상태여도) 그래서 Connected상태인거만 따로 뺌
//        List<CareRelation> myRelation = user.getAsProtectors().stream().filter(c->c.getLinkStatus()==LinkStatus.CONNECTED).toList();
//        boolean existDependent =false; //피보호자 맞는지 아닌지
//        List<HealthData> dependentData = new ArrayList<>();
//        for(CareRelation c : myRelation){
//            if(c.getDependent().getId()==dto.getDependentId()) {
//                existDependent = true;
//                dependentData = c.getDependent().getMyHealthData();
//            }
//        }
//        if(!existDependent){
//            throw new IllegalArgumentException("피보호자로 등록되지 않은 유저입니다");
//        }
//        //사용자가 입력한 주(날짜)의 월요일의 다음주 월요일에 만들어진거 찾아야하니까
//        LocalDate targetDate = LocalDate.parse(dto.getSelectDate()).with(DayOfWeek.MONDAY).plusWeeks(1);
//        HealthData targetData = dependentData.stream().filter(c->c.getCreatedDate().equals(targetDate)).filter(c->c.getDataType()==DataType.WEEKAVG)
//                .findFirst().orElseThrow(()->new EntityNotFoundException("해당 주간 데이터가 존재하지 않습니다"));
//        return targetData.toListDtoFromEntity(dependent.getProfileImage());
//    }
//
////    10. 내 피보호자 특정 주 평균헬스데이터 조회
//    public HealthDataListDto myDependentMonthHealthData(String loginId, SelectDateAndDepReqDto dto){
//        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//        User dependent = userRepository.findByIdAndDelYN(dto.getDependentId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
//       //CareRelation객체는 유저와 양방향 매핑으로 자동으로 유저의 칼럼에 추가됨.(수락하지않은 상태여도) 그래서 Connected상태인거만 따로 뺌
//        List<CareRelation> myRelation = user.getAsProtectors().stream().filter(c->c.getLinkStatus()==LinkStatus.CONNECTED).toList();
//        boolean existDependent =false;
//        List<HealthData> dependentData = new ArrayList<>();
//        for(CareRelation c : myRelation){
//            if(c.getDependent().getId()==dto.getDependentId()) {
//                existDependent = true;
//                dependentData = c.getDependent().getMyHealthData();
//            }
//        }
//       if(!existDependent){
//           throw new IllegalArgumentException("피보호자로 등록되지 않은 유저입니다");
//       }
//       //사용자가 입력한 달(날짜)에다 1개월 더하고 그달의 1일에 만들어진 월별데이터 찾아야하니까
//       LocalDate targetDate = LocalDate.parse(dto.getSelectDate()).plusMonths(1).withDayOfMonth(1);
//       HealthData targetData = dependentData.stream().filter(c->c.getCreatedDate().equals(targetDate)).filter(c->c.getDataType()==DataType.MONTHAVG)
//                                .findFirst().orElseThrow(()->new EntityNotFoundException("해당 월간 데이터가 존재하지 않습니다"));
//       return targetData.toListDtoFromEntity(dependent.getProfileImage());
//    }

//    11.헬스데이터 올인원 조회

    public HealthDataListDto allInOne(String loginId,SelectAllInOneReqDto dto){
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        //로그인한 유저의 피보호자 리스트 꺼내기 //보호자 리스트 꺼내기
        List<CareRelation> dependentsOfUser = loginUser.getAsProtectors().stream().filter(c->c.getLinkStatus()==LinkStatus.CONNECTED).toList();
        List<User> dependentUsers = dependentsOfUser.stream().map(c->c.getDependent()).toList();
        List<CareRelation> protectorsOfUsers = loginUser.getAsDependents().stream().filter(c->c.getLinkStatus()==LinkStatus.CONNECTED).toList();
        List<User> protectorUsers = protectorsOfUsers.stream().map(c->c.getProtector()).toList();
        //사용자가 조회하려는 건강데이터의 주인이 나 혹은 나의 피보호자인지 확인하는 boolean
        boolean isMyId = dto.getLoginId().equals(loginId);
        boolean isMyDependent = dependentUsers.stream().anyMatch(u->u.getLoginId().equals(dto.getLoginId())); //anyMatch는 조건을 만족하는 항목이 하나라도 있으면 true를 반환
        boolean isMyProtector = protectorUsers.stream().anyMatch(u->u.getLoginId().equals(dto.getLoginId()));

        //즉 프론트에서 헬스데이터를 조회하려는 아이디가 내꺼거나 혹은 내피보호자,보호자로 연결되어있는 사람이 아니라면 볼 수 없게 막아놓은 것
        if(!isMyId && !isMyDependent && !isMyProtector){
            throw new IllegalArgumentException("잘못된 입력입니다");
        }

        User selectedUser = null;
        if(isMyId){
            selectedUser = loginUser;
        } else{
            selectedUser = userRepository.findByLoginIdAndDelYN(dto.getLoginId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        }

        LocalDate today = LocalDate.now();
        LocalDate selectedDate =LocalDate.parse(dto.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DataType selectedType = DataType.valueOf(dto.getType());
//   일단 중단
        Optional<HealthData> dataOpt = healthDataRepository.findByUserIdAndCreatedDateAndDataType(selectedUser.getId(),selectedDate,selectedType);
       //내가 실시간으로 오늘  상대방의 데이터를 보려는데 없다면, 즉 상대방의 데이터중 오늘 실시간 데이터를 보려고 할 때 fcm메세지를 보내 데이터를 받아옴
        if(dataOpt.isEmpty() && selectedType ==DataType.DAY && selectedDate.equals(today)){
           String token = selectedUser.getFireBaseToken();
           fireBaseService.sendHealthSyncReq(token);
            try {
                Thread.sleep(1000); // 3초 대기
            } catch (InterruptedException ignored) {}
            dataOpt =healthDataRepository.findByUserIdAndCreatedDateAndDataType(selectedUser.getId(),selectedDate,selectedType);
        }

        HealthData selectedData = dataOpt.orElseThrow(()->new EntityNotFoundException("해당 데이터가 존재하지 않습니다"));

        //헬스데이터랑 타겟칼로리랑 타켓 걸음 같이 리턴
        int targetCalories =0;
        int targetSteps =0;
        UserDetailHealthInfo info = selectedUser.getUserDetailHealthInfo();
        if(info!= null){
                targetCalories = info.getTargetCalory();
                targetSteps= info.getTargetStep();
        }

        return selectedData.toListDtoFromEntity(selectedUser.getProfileImage(),targetCalories,targetSteps);

    }




}