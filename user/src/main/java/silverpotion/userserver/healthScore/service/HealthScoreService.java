package silverpotion.userserver.healthScore.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import silverpotion.userserver.careRelation.domain.CareRelation;
import silverpotion.userserver.careRelation.domain.LinkStatus;
import silverpotion.userserver.healthData.domain.DataType;
import silverpotion.userserver.healthData.domain.HealthData;
import silverpotion.userserver.healthData.reopisitory.HealthDataRepository;
import silverpotion.userserver.healthScore.domain.HealthScore;
import silverpotion.userserver.healthScore.domain.HealthScoreUtils;
import silverpotion.userserver.healthScore.domain.Type;
import silverpotion.userserver.healthScore.dtos.HealthScoreMakeReqDto;
import silverpotion.userserver.healthScore.dtos.HealthScoreResDto;
import silverpotion.userserver.healthScore.repository.HealthScoreRepository;
import silverpotion.userserver.user.domain.DelYN;
import silverpotion.userserver.user.domain.User;
import silverpotion.userserver.user.repository.UserRepository;
import silverpotion.userserver.userDetailHealthInfo.domain.UserDetailHealthInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HealthScoreService {

    private final HealthScoreRepository healthScoreRepository;
    private final UserRepository userRepository;
    private final HealthDataRepository healthDataRepository;

    public HealthScoreService(HealthScoreRepository healthScoreRepository, UserRepository userRepository, HealthDataRepository healthDataRepository) {
        this.healthScoreRepository = healthScoreRepository;
        this.userRepository = userRepository;
        this.healthDataRepository = healthDataRepository;
    }

//  1.헬스점수 만들기
    public HealthScoreResDto makingHealthScore(String loginId, HealthScoreMakeReqDto dto){
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));

        //로그인한 유저의 피보호자와 보호자 리스트
        List<CareRelation> dependents = loginUser.getAsProtectors().stream().filter(c->c.getLinkStatus()== LinkStatus.CONNECTED).toList();
        List<User> dependentUsers = dependents.stream().map(c->c.getDependent()).toList();
        List<CareRelation> protectors = loginUser.getAsDependents().stream().filter(c->c.getLinkStatus()== LinkStatus.CONNECTED).toList();
        List<User> protectorUsers =protectors.stream().map(c->c.getProtector()).toList();

        boolean isMyId = dto.getUserId().equals(loginId); //사용자가 조회하려는 데이터가 내꺼인 경우
        boolean isMyDependet = dependentUsers.stream().anyMatch(u->u.getLoginId().equals(dto.getUserId())); //사용자가 조회하려는 데이터가 내 피보호자일 경우
        boolean isMyProtector = protectorUsers.stream().anyMatch(u->u.getLoginId().equals(dto.getUserId())); //사용자가 조회하려는 데이터가 내 보호자일 경우

        if(!isMyId && !isMyDependet &&!isMyProtector){
            throw new IllegalArgumentException("연결관계가 없는 유저의 데이터는 확인할 수 없습니다.");
        }
        //클라이언트가 확인하려는 데이터가 나면 내 유저객체를 불러오고 다른 사람이면 다른 사람 유저객체를 불러오는데 불러와서 건강상세정보(조사)를 완료한 인물인지 아닌지 확인 아니라면 점수 발급 불가
        User selectedUser = null;
        if(isMyId){
            selectedUser = loginUser;

        } else{
            selectedUser = userRepository.findByLoginIdAndDelYN(dto.getUserId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("선택된 회원은 없는 회원입니다"));

        }
        if(selectedUser.getUserDetailHealthInfo()==null){
            throw new IllegalArgumentException("건강상세정보를 등록하지 않아 이용할 수 없습니다");
        }

       LocalDate selectedDate = LocalDate.parse(dto.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Type type = Type.valueOf(dto.getType());
        DataType dataType = DataType.valueOf(dto.getType());
        //기존에 이미 조회하려는 헬스스코어가 있는 지 확인
        Optional<HealthScore> optionalHealthScore = healthScoreRepository.findByUserIdAndCreatedDateAndType(selectedUser.getId(), selectedDate, type);
        HealthScore healthScore;

        //헬스스코어가 있다면 가져오고
        if(optionalHealthScore.isPresent()){
            healthScore = healthScoreRepository.findByUserIdAndCreatedDateAndType(selectedUser.getId(), selectedDate, type).orElseThrow(() -> new EntityNotFoundException("데이터가 없습니다"));
        } else{
           //헬스스코어가 없다면 생성
            HealthData healthData = selectedUser.getMyHealthData().stream().filter(h->h.getDataType().equals(dataType)).filter(c->c.getCreatedDate().equals(selectedDate)).findFirst().orElseThrow(()->new EntityNotFoundException("데이터가 없습니다2"));
//            방법 2
//            HealthData healthData = healthDataRepository.findByUserIdAndCreatedDateAndDataType(selectedUser.getId(), selectedDate, dataType).orElseThrow(()-> new EntityNotFoundException("데이터가 없습니다"));
            UserDetailHealthInfo info = selectedUser.getUserDetailHealthInfo();

            int activityScore =  HealthScoreUtils.whatsMyActivityScore(healthData,info,selectedUser);
            int bodyScore =HealthScoreUtils.whatsMybodyScore(healthData,info,selectedUser);
            int habitScore =HealthScoreUtils.whatsMyHabitScore(info);
            int totalScore = HealthScoreUtils.totalScore(activityScore,bodyScore,habitScore);

            healthScore =  HealthScore.builder().totalScore(totalScore).activityScore(activityScore).bodyScore(bodyScore)
                    .habitScore(habitScore).type(type).user(selectedUser).createdDate(selectedDate).build();

            healthScoreRepository.save(healthScore);
            selectedUser.getHealthScores().add(healthScore);
        }

        return healthScore.toDto();

    }








}
