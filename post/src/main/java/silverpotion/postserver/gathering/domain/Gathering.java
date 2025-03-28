package silverpotion.postserver.gathering.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.postserver.common.domain.BaseTimeEntity;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.gatheringCategory.domain.GatheringCategory;
import silverpotion.postserver.gatheringCategory.domain.GatheringDetail;
import silverpotion.postserver.post.domain.Post;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class Gathering extends BaseTimeEntity {
    //아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //모임 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private GatheringCategory category;
    //모임명
    @Column(nullable = false)
    private String gatheringName;
    //활동지역(이 부분 더 공부필요 api를 가져오거나 공공데이터포탈에서 전국 지역데 이터 다운받아서 수동으로 db에 넣거나)
    private String region;
    //모임소개
    private String introduce;
    //모임 프로필 사진
    private String imageUrl;
    @Builder.Default
    @Enumerated(EnumType.STRING )
    private DelYN delYN=DelYN.N;

    @OneToMany(mappedBy = "gathering" )
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "gathering" )
    @Builder.Default
    private List<GatheringDetail> gatheringDetails = new ArrayList<>();
}
