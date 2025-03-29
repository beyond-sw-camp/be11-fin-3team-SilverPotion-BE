package silverpotion.userserver.careRelation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import silverpotion.userserver.careRelation.domain.CareRelation;
import silverpotion.userserver.careRelation.domain.LinkStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareRelationRepository extends JpaRepository<CareRelation,Long> {
    Optional<CareRelation> findByIdAndLinkStatus(Long id, LinkStatus linkStatus);
    List<CareRelation> findByDependentIdAndLinkStatus(Long id, LinkStatus linkStatus);
}
