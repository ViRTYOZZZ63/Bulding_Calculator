package backend.back.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculationResultResponse {
    private Long calculationId;
    private List<CalculationElementResponse> elements;
    private BigDecimal grandTotal;
}