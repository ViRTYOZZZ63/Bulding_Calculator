package backend.back.dto.response;

import backend.back.entity.enums.ElementType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CalculationElementResponse {
    private Long id;
    private ElementType elementType;
    private Map<String, Object> inputParams;
    private List<ResultItemResponse> resultItems;
    private BigDecimal totalCost;

    @Data
    public static class ResultItemResponse {
        private String section;
        private String materialName;
        private String unit;
        private Double quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}