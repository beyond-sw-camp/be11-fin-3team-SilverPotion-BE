package silverpotion.postserver.gathering.service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
//import org.opensearch.client.RestHighLevelClient;
//import org.opensearch.client.RestHighLevelClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.common.service.ImageService;
import silverpotion.postserver.gathering.chatDto.ChatRoomCreateRequest;
import silverpotion.postserver.gathering.chatDto.ChatRoomResponse;
import silverpotion.postserver.gathering.domain.Gathering;
import silverpotion.postserver.gathering.domain.GatheringPeople;
import silverpotion.postserver.gathering.domain.Status;
import silverpotion.postserver.gathering.dto.*;
import silverpotion.postserver.gathering.repository.GatheringPeopleRepository;
import silverpotion.postserver.gatheringCategory.domain.GatheringDetail;
import silverpotion.postserver.gatheringCategory.repository.GatheringCategoryDetailRepository;
import silverpotion.postserver.gatheringCategory.repository.GatheringCategoryRepository;
import silverpotion.postserver.gathering.repository.GatheringRepository;
import silverpotion.postserver.gatheringCategory.domain.GatheringCategory;
import silverpotion.postserver.gatheringCategory.repository.GatheringDetailRepository;
import silverpotion.postserver.gatheringVector.domain.GatheringVector;
import silverpotion.postserver.gatheringVector.domain.GatheringVectorUtils;
import silverpotion.postserver.gatheringVector.dtos.GatheringVectorCreateDto;
import silverpotion.postserver.gatheringVector.repository.GatheringVectorRepository;
import silverpotion.postserver.notification.dto.NotificationMessageDto;
import silverpotion.postserver.notification.service.NotificationEventPublisher;
import silverpotion.postserver.notification.service.NotificationProducer;
import silverpotion.postserver.post.feignClient.UserClient;
import silverpotion.postserver.post.dtos.UserProfileInfoDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringCategoryRepository gatheringCategoryRepository;
    private final UserClient userClient;
    private final GatheringCategoryDetailRepository gatheringCategoryDetailRepository;
    private final GatheringDetailRepository gatheringDetailRepository;
    private final GatheringPeopleRepository gatheringPeopleRepository;
    private final ImageService imageService;
    private final ChatFeignClient chatFeignClient;
    private final NotificationEventPublisher notificationEventPublisher;
    private final NotificationProducer notificationProducer;
    private final GatheringVectorRepository gatheringVectorRepository;
//    private final OpenSearchService openSearchService;
//    @Autowired
//    private RestHighLevelClient client;


    public GatheringService(GatheringRepository gatheringRepository, GatheringCategoryRepository gatheringCategoryRepository, UserClient userClient, GatheringCategoryDetailRepository gatheringCategoryDetailRepository, GatheringDetailRepository gatheringDetailRepository, GatheringPeopleRepository gatheringPeopleRepository, ImageService imageService,
//            , OpenSearchService openSearchService
                            ChatFeignClient chatFeignClient, NotificationEventPublisher notificationEventPublisher, NotificationProducer notificationProducer, GatheringVectorRepository gatheringVectorRepository) {
        this.gatheringRepository = gatheringRepository;
        this.gatheringCategoryRepository = gatheringCategoryRepository;
        this.userClient = userClient;
        this.gatheringCategoryDetailRepository = gatheringCategoryDetailRepository;
        this.gatheringDetailRepository = gatheringDetailRepository;
        this.gatheringPeopleRepository = gatheringPeopleRepository;
        this.imageService = imageService;
//        this.openSearchService = openSearchService;
        this.chatFeignClient = chatFeignClient;
        this.notificationEventPublisher = notificationEventPublisher;
        this.notificationProducer = notificationProducer;
        this.gatheringVectorRepository = gatheringVectorRepository;
    }

//    @PostConstruct
//    public void init() {
//        try {
//            OpenSearchIndexUtil util = new OpenSearchIndexUtil(client);
//            util.createGatheringIndexIfNotExists();
//            util.createMeetingIndexIfNotExists();
//            System.out.println("✅ OpenSearch 인덱스 확인 완료 (gathering, meeting)");
//        } catch (Exception e) {
//            System.err.println("❌ OpenSearch 인덱스 생성 중 오류 발생: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }


    // 모임 생성
    public Long gatheringCreateWithChat(GatheringCreateDto dto, String loginId, List<Long> gatheringCategoryDetailIds) {
        if (gatheringRepository.findByGatheringNameAndDelYN(dto.getGatheringName(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 모임명입니다");
        }

        GatheringCategory gatheringCategory = gatheringCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리 ID입니다."));

        Long leaderId = userClient.getUserIdByLoginId(loginId);

        if (gatheringRepository.countByLeaderIdAndDelYN(leaderId, DelYN.N) >= 8) {
            throw new IllegalArgumentException("최대 모임 개수를 초과했습니다.");
        }

        // 🔹 1. 채팅방 생성 or 재사용
        ChatRoomResponse chatRoom;
        try {
            chatRoom = chatFeignClient.findExistingGroupRoom(dto.getGatheringName(), leaderId);
        } catch (FeignException.NotFound e) {
            ChatRoomCreateRequest chatRequest = new ChatRoomCreateRequest();
            chatRequest.setTitle(dto.getGatheringName());
            chatRequest.setUserIds(List.of(leaderId));
            chatRequest.setType("GROUP");

            chatRoom = chatFeignClient.createGroupRoom(chatRequest);
        }

        // 🔹 2. 모임 저장

        Gathering gathering = dto.toEntity(gatheringCategory, leaderId);
        gathering.setChatRoomId(chatRoom.getId());
        gatheringRepository.save(gathering);

        dto.setGatheringId(gathering.getId());

        // 🔹 3. 디테일 저장
        List<GatheringDetail> details = gatheringCategoryDetailIds.stream()
                .map(id -> new GatheringDetail(gathering, gatheringCategoryDetailRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 세부카테고리 ID입니다."))))
                .collect(Collectors.toList());

        gatheringDetailRepository.saveAll(details);
        log.info("모임생성정보 id:{}, name:{}, chatRoomId:{}",gathering.getId(),gathering.getGatheringName(),gathering.getChatRoomId());


        // 4.모임 벡터값 생성
        for(GatheringDetail g : details){
            gathering.getGatheringDetails().add(g);
        }

        GatheringVectorCreateDto vectorCreateDto = GatheringVectorUtils.makingGatheringVector(gathering);
        double empathySupport = GatheringVectorUtils.normalize(vectorCreateDto.getEmpathySupport(),30);
        double achievementSupport =GatheringVectorUtils.normalize(vectorCreateDto.getAchievementSupport(),30);
        double connectivitySupport = GatheringVectorUtils.normalize(vectorCreateDto.getConnectivitySupport(),20);
        double energySupport =  GatheringVectorUtils.normalize(vectorCreateDto.getAchievementSupport(),20);
        GatheringVector gatheringVector = GatheringVector.builder().empathySupport(empathySupport).connectivitySupport(connectivitySupport)
                .achievementSupport(achievementSupport).energySupport(energySupport).gathering(gathering).build();
        gatheringVectorRepository.save(gatheringVector);



        return gathering.getId();











    }

    // 모임 수정
    public void updateGathering(String loginId, Long gatheringId, GatheringUpdateDto dto) {
        // 로그인 ID로 userId 조회
        Long userId = userClient.getUserIdByLoginId(loginId);

        // Gathering 조회 및 모임장 검증
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다."));

        if (!gathering.getLeaderId().equals(userId)) {
            throw new IllegalArgumentException("모임장만 수정할 수 있습니다.");
        }

        // 이미지 업로드 (새로운 이미지가 있는 경우 S3 업데이트)
        String imageUrl = gathering.getImageUrl();
        MultipartFile imageFile = dto.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = imageService.uploadImage(imageFile);
        }

        // Meeting 정보 업데이트 (null 체크 후 수정)
        if (dto.getGatheringName() != null) gathering.setGatheringName(dto.getGatheringName());
        if (dto.getIntroduce() != null) gathering.setIntroduce(dto.getIntroduce());
        if (dto.getMaxPeople() != null) gathering.setMaxPeople(dto.getMaxPeople());
        gathering.setImageUrl(imageUrl);

        gatheringRepository.save(gathering);

        // OpenSearch Index 저장
//        openSearchService.indexGathering(gathering);
    }

    // 내 모임 조회
    public List<GatheringInfoDto> getMyGatherings(String loginId) {
        // loginId로 userId 조회
        Long userId = userClient.getUserIdByLoginId(loginId);

        // GatheringPeople 테이블에서 사용자가 가입한 gatheringId 가져오기
        List<Long> gatheringIds = gatheringPeopleRepository.findByUserIdAndStatus(userId, Status.ACTIVATE)
                .stream()
                .map(gp -> gp.getGathering().getId())
                .collect(Collectors.toList());

        // Gathering 테이블에서 해당 gathering 정보 가져오기
        return gatheringRepository.findByIdIn(gatheringIds)
                .stream()
                .map(gathering -> {
                    // 모임 카테고리명 가져오기
                    String category = gathering.getGatheringCategory() != null
                            ? gathering.getGatheringCategory().getName()
                            : "미분류";

                    // 현재 모임 인원 수 가져오기
                    Long peopleCount = gatheringPeopleRepository.countByGatheringIdAndStatusActivate(gathering.getId());

                    return new GatheringInfoDto(
                            gathering.getId(),
                            gathering.getGatheringName(),
                            gathering.getImageUrl(),
                            gathering.getRegion(),
                            gathering.getMaxPeople(),
                            category,
                            gathering.getIntroduce(),
                            peopleCount,
                            gathering.getLeaderId(),
                            gathering.getChatRoomId()
                    );
                })
                .collect(Collectors.toList());
    }

    // 모임 참여자 수 조회
    public GatheringPeopleCountDto getActivePeopleCount(Long gatheringId) {
        Long count = gatheringPeopleRepository.countByGatheringIdAndStatusActivate(gatheringId);
        return new GatheringPeopleCountDto(gatheringId, count);
    }

    // 모임 상세조회
    public GatheringInfoDto getGatheringById(Long gatheringId) {
        Gathering gathering = gatheringRepository.findByIdAndDelYN(gatheringId,DelYN.N)
                .orElseThrow(() -> new EntityNotFoundException("해당 모임을 찾을 수 없습니다."));

        // 카테고리명 조회
        String category = gathering.getGatheringCategory() != null
                ? gathering.getGatheringCategory().getName()
                : "미분류";

        // 현재 모임 인원 수 조회
        Long peopleCount = gatheringPeopleRepository.countByGatheringIdAndStatusActivate(gatheringId);

        return new GatheringInfoDto(
                gathering.getId(),
                gathering.getGatheringName(),
                gathering.getImageUrl(),
                gathering.getRegion(),
                gathering.getMaxPeople(),
                category,
                gathering.getIntroduce(),
                peopleCount,
                gathering.getLeaderId(),
                gathering.getChatRoomId()
        );
    }

    // 모임 검색
    public List<GatheringInfoDto> searchGatherings(String category, String gatheringName, String region, String categoryDetail) {
        List<Gathering> gatherings;

        // Step 1: categoryDetailName으로 gatheringId 리스트 추출
        List<Long> gatheringIdsWithDetail;
        if (categoryDetail != null && !categoryDetail.isBlank()) {
            gatheringIdsWithDetail = gatheringDetailRepository.findByGatheringCategoryDetail_NameContaining(categoryDetail)
                    .stream()
                    .map(gd -> gd.getGathering().getId())
                    .distinct()
                    .toList();
        } else {
            gatheringIdsWithDetail = null;
        }

        // Step 2: 조건별로 Gathering 가져오기
        gatherings = gatheringRepository.findByDelYN(DelYN.N).stream()
                .filter(g -> (category == null || g.getGatheringCategory().getName().equals(category)) &&
                        (gatheringName == null || g.getGatheringName().contains(gatheringName)) &&
                        (region == null || g.getRegion().contains(region)) &&
                        (categoryDetail == null || gatheringIdsWithDetail.contains(g.getId())))
                .toList();

        // Step 3: DTO로 변환
        return gatherings.stream()
                .map(gathering -> new GatheringInfoDto(
                        gathering.getId(),
                        gathering.getGatheringName(),
                        gathering.getImageUrl(),
                        gathering.getRegion(),
                        gathering.getMaxPeople(),
                        gathering.getGatheringCategory() != null ? gathering.getGatheringCategory().getName() : "미분류",
                        gathering.getIntroduce(),
                        gatheringPeopleRepository.countByGatheringIdAndStatusActivate(gathering.getId()),
                        gathering.getLeaderId(),
                        gathering.getChatRoomId()
                ))
                .collect(Collectors.toList());
    }

    // 모임별 userList
    public List<GatheringPeopleDto> getGatheringUserList(Long gatheringId) {
//        List<GatheringPeople> gatheringPeopleList = gatheringPeopleRepository.findByGatheringIdAndStatus(gatheringId, Status.ACTIVATE);
        List<GatheringPeople> gatheringPeopleList = gatheringPeopleRepository.findByGatheringId(gatheringId);

        return gatheringPeopleList.stream().map(gatheringPeople -> {
            // User 정보 조회
            UserProfileInfoDto profileInfo = userClient.getUserProfileInfo(gatheringPeople.getUserId());
            String loginId = userClient.getLoginIdByUserId(gatheringPeople.getUserId());

            return new GatheringPeopleDto(
                    gatheringPeople.getId(),
                    gatheringPeople.getGathering().getId(),
                    gatheringPeople.getUserId(),
                    profileInfo.getNickname(),
                    profileInfo.getProfileImage(),
                    gatheringPeople.getGreetingMessage(),
                    gatheringPeople.getStatus().name(),  // Enum -> String 변환
                    gatheringPeople.getCreatedTime(),
                    loginId
            );
        }).collect(Collectors.toList());
    }

    // 모임 가입
    public void createGatheringPeople(GatheringPeopleCreateDto dto, String loginId) {
        Long userId = userClient.getUserIdByLoginId(loginId);

        Gathering gathering = gatheringRepository.findById(dto.getGatheringId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Gathering ID입니다."));

        Optional<GatheringPeople> existingMembership = gatheringPeopleRepository.findByGatheringIdAndUserId(dto.getGatheringId(), userId);

        if (existingMembership.isPresent()) {
            GatheringPeople gatheringPeople = existingMembership.get();
            Status status = gatheringPeople.getStatus();

            switch (status) {
                case WAIT:
                    throw new IllegalStateException("가입 대기중입니다.");
                case ACTIVATE:
                    throw new IllegalStateException("이미 가입된 모임입니다.");
                case BAN:
                    throw new IllegalStateException("추방된 모임입니다.");
                case DEACTIVATE:
                    // DEACTIVATE 상태면 상태를 WAIT으로 바꾸고 저장
                    gatheringPeople.setStatus(Status.WAIT);
                    gatheringPeople.setGreetingMessage(dto.getGreetingMessage()); // 인사말도 갱신 가능
                    gatheringPeopleRepository.save(gatheringPeople);
                    return;
            }
        }

        GatheringPeople gatheringPeople = GatheringPeople.builder()
                .gathering(gathering)
                .userId(userId)
                .greetingMessage(dto.getGreetingMessage())
                .status(Status.WAIT) // 기본 상태
                .build();
        // DB 저장
        gatheringPeopleRepository.save(gatheringPeople);

        // 알림발송
        Long gatheringLeaderId = gathering.getLeaderId();
        String gatheringLeaderLoginId = userClient.getLoginIdByUserId(gatheringLeaderId);
        String userNickname = userClient.getNicknameByUserId(userId);
        System.out.println("모임장 id: "+gatheringLeaderId+" 모임장 로그인id: " + gatheringLeaderLoginId + " 신청자닉네임: "+userNickname);

        notificationProducer.sendNotification(NotificationMessageDto.builder()
                .loginId(gatheringLeaderLoginId)
                .title("모임 가입 요청")
                .content(userNickname + "님이 '" + gathering.getGatheringName() + "' 모임에 가입 요청을 보냈습니다.")
                .type("JOIN_REQUEST")
                .referenceId(dto.getGatheringId())
                .build());


    }

    // 모임원 상태 변경
    public void updateGatheringPeopleStatus(Long gatheringPeopleId, String ownerLoginId, GatheringPeopleUpdateDto dto) {
        Long ownerUserId = userClient.getUserIdByLoginId(ownerLoginId);

        // GatheringPeople 조회
        GatheringPeople gatheringPeople = gatheringPeopleRepository.findById(gatheringPeopleId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 GatheringPeople ID입니다."));
        String gatheringPeopleLoginId = userClient.getLoginIdByUserId(gatheringPeople.getUserId());
        // 모임 조회
        Gathering gathering = gatheringPeople.getGathering();

        // 요청자가 해당 모임의 모임장인지 검증
        if (!gathering.getLeaderId().equals(ownerUserId)) {
            throw new IllegalStateException("해당 모임의 모임장만 상태를 변경할 수 있습니다.");
        }

        // 상태 변경
        gatheringPeople.updateStatus(dto.getStatus());

        gatheringPeopleRepository.save(gatheringPeople);
        if (gatheringPeople.getStatus() == Status.ACTIVATE) {
            // 가입 승인시 알림 발송
            NotificationMessageDto notification = NotificationMessageDto.builder()
                    .loginId(gatheringPeopleLoginId) // 또는 userClient로 얻은 loginId
                    .title("가입 승인 완료")
                    .content("' 모임의 가입 요청이 수락되었습니다.")
                    .type("JOIN_APPROVED")
                    .referenceId(gathering.getId())
                    .build();

            notificationProducer.sendNotification(notification);
        }else if (dto.getStatus() == Status.DEACTIVATE) {
            // 탈퇴/해체 알림
            NotificationMessageDto notification = NotificationMessageDto.builder()
                    .loginId(gatheringPeopleLoginId)
                    .title("모임 비활성화 처리")
                    .content(" 모임에서 탈퇴 또는 해체 처리되었습니다.")
                    .type("DEACTIVATED")
                    .referenceId(gathering.getId())
                    .build();

            notificationProducer.sendNotification(notification);

        } else if (dto.getStatus() == Status.BAN) {
            // 추방 알림
            NotificationMessageDto notification = NotificationMessageDto.builder()
                    .loginId(gatheringPeopleLoginId)
                    .title("모임에서 추방됨")
                    .content(" 모임에서 강제 탈퇴 처리되었습니다.")
                    .type("BANNED")
                    .referenceId(gathering.getId())
                    .build();

            notificationProducer.sendNotification(notification);

            // 채팅 참여자 제거 로직 (선택)
            // chatFeignClient.removeParticipant(...);
        }
    }

    // 모임장 양도
    public void changeLeader(Long gatheringId, String loginId, LeaderChangeDto dto) {
        // 로그인한 사용자의 ID 조회
        Long currentLeaderId = userClient.getUserIdByLoginId(loginId);

        // 해당 모임 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Gathering ID입니다."));

        // 현재 로그인한 사용자가 모임장인지 검증
        if (!gathering.getLeaderId().equals(currentLeaderId)) {
            throw new IllegalArgumentException("모임장만이 모임장을 변경할 수 있습니다.");
        }

        // 새로운 모임장이 될 사람이 ACTIVATE 상태인지 검증
        boolean isActiveMember = gatheringPeopleRepository.existsByGatheringIdAndUserIdAndStatus(
                gatheringId, dto.getUserId(), Status.ACTIVATE);

        if (!isActiveMember) {
            throw new IllegalArgumentException("새로운 모임장은 해당 모임의 활성화된 멤버여야 합니다.");
        }

        // 새로운 모임장으로 변경
        gathering.changeLeader(dto.getUserId());
        gatheringRepository.save(gathering);

        // ✅ 새로운 모임장에게 알림 발송
        String newLeaderLoginId = userClient.getLoginIdByUserId(dto.getUserId());

        NotificationMessageDto notification = NotificationMessageDto.builder()
                .loginId(newLeaderLoginId)
                .title("모임장 위임 알림")
                .content("회원님이 '" + gathering.getGatheringName() + "' 모임의 새로운 모임장이 되었습니다.")
                .type("LEADER_CHANGED")
                .referenceId(gatheringId)
                .build();

        notificationProducer.sendNotification(notification);
    }

    // 모임 탈퇴
    public void withdrawFromGathering(Long gatheringId, String loginId) {
        Long userId = userClient.getUserIdByLoginId(loginId);

        GatheringPeople gatheringPeople = gatheringPeopleRepository.findByGatheringIdAndUserId(gatheringId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참여 정보가 존재하지 않습니다."));

        gatheringPeople.setStatus(Status.DEACTIVATE);
        gatheringPeople.setUpdatedTime(LocalDateTime.now());
        // save 생략 가능 (영속성 컨텍스트 관리하므로)
    }

    // 모임 해체
    public void disbandGathering(Long gatheringId, String loginId) {
        Long userId = userClient.getUserIdByLoginId(loginId);

        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        if (!gathering.getLeaderId().equals(userId)) {
            throw new IllegalStateException("해당 모임의 모임장만 모임을 해체할 수 있습니다.");
        }

        // 모임 해체 처리
        gathering.setDelYN(DelYN.Y);
        gathering.setUpdatedTime(LocalDateTime.now());

        // 그룹 채팅방 삭제 요청 (Del.Y 처리)
        chatFeignClient.deleteChatRoom(gathering.getChatRoomId());

        // 해당 모임의 모든 참가자 상태 DEACTIVATE 처리
        List<GatheringPeople> peopleList = gatheringPeopleRepository.findAllByGatheringId(gatheringId);
        for (GatheringPeople person : peopleList) {
            person.setStatus(Status.DEACTIVATE);
            person.setUpdatedTime(LocalDateTime.now());
        }
        // 모임 회원들에게 해체 알림 발송
        for (GatheringPeople person : peopleList) {
            String memberLoginId = userClient.getLoginIdByUserId(person.getUserId());

            NotificationMessageDto notification = NotificationMessageDto.builder()
                    .loginId(memberLoginId)
                    .title("모임 해체 알림")
                    .content("'" + gathering.getGatheringName() + " 모임이 해체되었습니다.")
                    .type("GATHERING_DISBANDED")
                    .referenceId(gatheringId)
                    .build();

            notificationProducer.sendNotification(notification);
        }
        // OpenSearch Index 저장
//        openSearchService.indexGathering(gathering);
    }

//    // opensearch
//    public List<GatheringSearchResultDto> searchGatherings(GatheringSearchRequest request) {
//        return openSearchService.searchGatherings(request);
//    }



   public List<GatheringInfoDtoForUserServiceDto> fiveRecommendedGatherings(List<Long> gatheringIds){

       List<Gathering>fiveList = gatheringRepository.findByIdIn(gatheringIds);

      return  fiveList.stream().map(gathering -> {
           //모임 카테고리명
           String category = gathering.getGatheringCategory() !=null ? gathering.getGatheringCategory().getName() : "미분류";
           //세부카테고리명 최대 2개까지니까 리스트로 가지고 오기
           List<String> detailCategory = gathering.getGatheringDetails().stream().map(gd -> gd.getGatheringCategoryDetail().getName()).toList();
           //현재 모임 인원 수 가져오기
           Long peopleCount = gatheringPeopleRepository.countByGatheringIdAndStatusActivate(gathering.getId());

           return GatheringInfoDtoForUserServiceDto.builder()
                   .id(gathering.getId())
                   .gatheringName(gathering.getGatheringName())
                   .imageUrl(gathering.getImageUrl())
                   .region(gathering.getRegion())
                   .maxPeople(gathering.getMaxPeople())
                   .peopleCount(peopleCount)
                   .category(category)
                   .detailCategory(detailCategory)
                   .introduce(gathering.getIntroduce())
                   .build();
               }).toList();

    }








}
