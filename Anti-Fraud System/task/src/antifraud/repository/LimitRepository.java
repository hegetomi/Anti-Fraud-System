package antifraud.repository;

import antifraud.model.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LimitRepository extends JpaRepository<Limit,Long> {

    Optional<Limit> findFirstByOrderByAtDateDesc();


}
