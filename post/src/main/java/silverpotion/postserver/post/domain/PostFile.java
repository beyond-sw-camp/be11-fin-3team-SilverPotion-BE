package silverpotion.postserver.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PostFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String fileUrl;

    @Builder.Default
    private String isActive = "Y"; //삭제여부 N이면 삭제

    public PostFile(Post post, String fileUrl){
        this.post = post;
        this.fileUrl = fileUrl;
    }

}
