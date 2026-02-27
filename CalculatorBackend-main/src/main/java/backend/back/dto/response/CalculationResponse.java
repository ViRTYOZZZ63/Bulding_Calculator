package backend.back.dto.response;

import backend.back.entity.enums.CalculationStatus;
import backend.back.entity.enums.ElementType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CalculationResponse {
    private Long id;
    private Long clientId;
    private String constructionAddress;
    private CalculationStatus status;
    private LocalDateTime pricesFixedUntil;
    private LocalDateTime createdAt;
    private List<CalculationElementResponse> elements;
    private BigDecimal totalCost;
}