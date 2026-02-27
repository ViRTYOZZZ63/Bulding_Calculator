package backend.back.repository;

import backend.back.entity.Calculation;
import backend.back.entity.enums.CalculationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CalculationRepository extends JpaRepository<Calculation, Long> {
    List<Calculation> findAllByClientId(Long clientId);
    List<Calculation> findAllByStatusAndPricesFixedUntilBefore(
            CalculationStatus status, LocalDateTime dateTime
    );  // для задачи по истечению 10 дней
}
