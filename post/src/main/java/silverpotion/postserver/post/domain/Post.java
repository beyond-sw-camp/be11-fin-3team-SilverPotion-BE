package silverpotion.postserver.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.postserver.common.domain.BaseTimeEntity;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.gathering.domain.Gathering;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Post extends BaseTimeEntity {
    //아이디
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;
    //게시판
    @Enumerated(EnumType.STRING)
    private PostCategory postCategory;
    //글 제목
    @Column(nullable = false)
    private String title;
    //글 내용
    @Column(nullable = false)
    private String content;
    //삭제여부
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DelYN delYN=DelYN.N;
    //조회수
    private Integer viewCount;
    //좋아요 수
    private Integer likeCount;
    //소모임 아이디(이 게시글이 속하는 소모임의 아이디)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

}
