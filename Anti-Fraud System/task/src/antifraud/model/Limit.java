package antifraud.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "limit_transaction")
public class Limit {

    @Id
    private Long id;
    private Long allowedLimit;
    private Long manualLimit;
    private LocalDateTime atDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAllowedLimit() {
        return allowedLimit;
    }

    public void setAllowedLimit(Long allowedLimit) {
        this.allowedLimit = allowedLimit;
    }

    public Long getManualLimit() {
        return manualLimit;
    }

    public void setManualLimit(Long manualLimit) {
        this.manualLimit = manualLimit;
    }

    public LocalDateTime getAtDate() {
        return atDate;
    }

    public void setAtDate(LocalDateTime atDate) {
        this.atDate = atDate;
    }
}
