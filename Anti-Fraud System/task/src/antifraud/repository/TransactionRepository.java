package antifraud.repository;

import antifraud.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<TransactionRepository> findAllByRegionNotLikeAndLocalDateTimeBetween(String region, LocalDateTime from, LocalDateTime to);

    @Query(value = "SELECT COUNT(DISTINCT t.region) FROM Transaction t WHERE t.region <> ?1 AND t.number = ?2 AND t.localDateTime BETWEEN ?3 AND ?4")
    Long findDistinctByRegionNotLikeAndLocalDateTimeBetween(String region, String account, LocalDateTime from, LocalDateTime to);

    List<TransactionRepository> findAllByIpNotLikeAndLocalDateTimeBetween(String ip, Date from, Date to);

    @Query(value = "SELECT COUNT(DISTINCT t.ip) FROM Transaction t WHERE t.ip <> ?1 AND t.number = ?2 AND t.localDateTime BETWEEN ?3 AND ?4")
    Long findDistinctByIpNotLikeAndLocalDateTimeBetween(String ip, String account, LocalDateTime from, LocalDateTime to);

    List<Transaction> findByNumber(String number);
}
