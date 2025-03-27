package silverpotion.userserver.careRelation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import silverpotion.userserver.common.domain.BaseTimeEntity;
import silverpotion.userserver.user.domain.User;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class CareRelation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private User protecter;
    private User dependent;
    private LinkStatus linkStatus;



}
