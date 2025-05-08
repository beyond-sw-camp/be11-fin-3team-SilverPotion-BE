package silverpotion.postserver.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.postserver.comment.domain.Comment;
import silverpotion.postserver.common.domain.BaseTimeEntity;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.gathering.domain.Gathering;
import silverpotion.postserver.post.dtos.VotePostUpdateDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Vote extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteId; // 투표id

    private Long writerId; //회원서버에서 요청

    private String title;

    //게시판
    @Enumerated(EnumType.STRING)
    private PostCategory postCategory;

    //삭제여부
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DelYN delYn=DelYN.N;

    private String description;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteOptions> voteOptions = new ArrayList<>();//투표항목

    private boolean multipleChoice; //복수선택 여부

    @OneToMany(mappedBy = "vote", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteLike> voteLikes = new ArrayList<>();

    @Builder.Default
    @Column(name = "like_count",nullable = false)
    private Long likeCount = 0L;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PostStatus postStatus = PostStatus.draft;

    @Column(name = "close_time")
    private LocalDateTime closeTime;

    //소모임 아이디(이 게시글이 속하는 소모임의 아이디)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    public void update(Long userId,VotePostUpdateDto dto) {
        this.writerId = userId;
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.multipleChoice = dto.isMultipleChoice();
    }

    public void addVoteOption(VoteOptions option) {
        this.voteOptions.add(option);
        option.updateVote(this); // 주인 쪽에 연결
    }

    //  작성자를 한번만 지정할 수 있도록 제약
    public void assignWriter(Long userId) {
        if (this.writerId != null) {
            throw new IllegalStateException("작성자는 이미 설정되어 있습니다.");
        }
        this.writerId = userId;
    }

    public void changeStatus(PostStatus status) {
        this.postStatus = status;
    }

    public void setCloseTime() {
        this.closeTime = LocalDateTime.now().plusDays(2);
    }

    //좋아요 수 변경
    public void changeVoteLike(Long likeCount){
        this.likeCount = likeCount;
    }

    @PrePersist
    public void prePersist() {
        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
    }
}
