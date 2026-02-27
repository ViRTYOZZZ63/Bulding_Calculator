package backend.back.repository;

import backend.back.entity.CalculationResultItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalculationResultItemRepository extends JpaRepository<CalculationResultItem, Long> {
    List<CalculationResultItem> findAllByCalculationElementId(Long elementId);
    void deleteAllByCalculationElementId(Long elementId);  // при пересчёте
}
