package backend.back.mapper;

import backend.back.dto.response.CalculationElementResponse;
import backend.back.entity.CalculationElement;
import backend.back.entity.CalculationResultItem;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CalculationElementMapper {

    @Mapping(target = "resultItems", source = "resultItems")
    @Mapping(target = "totalCost", expression = "java(calcTotal(element))")
    CalculationElementResponse toResponse(CalculationElement element);

    CalculationElementResponse.ResultItemResponse toResultItemResponse(CalculationResultItem item);

    default BigDecimal calcTotal(CalculationElement element) {
        return element.getResultItems().stream()
                .map(i -> i.getTotalPrice() != null ? i.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}