package backend.back.repository;

import backend.back.entity.CalculationElement;
import backend.back.entity.enums.ElementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalculationElementRepository extends JpaRepository<CalculationElement, Long> {
    List<CalculationElement> findAllByCalculationId(Long calculationId);
    Optional<CalculationElement> findByCalculationIdAndElementType(
            Long calculationId, ElementType elementType
    );
}
