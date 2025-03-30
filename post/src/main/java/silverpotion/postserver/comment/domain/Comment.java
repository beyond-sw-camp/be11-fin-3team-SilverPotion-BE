package silverpotion.postserver.comment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.postserver.common.domain.BaseTimeEntity;
import silverpotion.postserver.common.domain.DelYN;
import silverpotion.postserver.post.domain.Post;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private Long userId;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> child = new ArrayList<>();

    @OneToMany(mappedBy = "comment" , cascade = CascadeType.ALL)
    private List<CommentLike> commentLike = new ArrayList<>();

    @Builder.Default
    private DelYN delYN = DelYN.N;


}

