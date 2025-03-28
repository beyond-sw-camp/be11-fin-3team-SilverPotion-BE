package silverpotion.postserver.gatheringCategory.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.postserver.gathering.domain.Gathering;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GatheringDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_category_detail_id")
    private GatheringCategoryDetail gatheringCategoryDetail;
}
