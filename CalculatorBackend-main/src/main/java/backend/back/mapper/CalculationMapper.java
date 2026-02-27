package backend.back.mapper;

import backend.back.dto.response.CalculationResponse;
import backend.back.entity.Calculation;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {CalculationElementMapper.class})
public interface CalculationMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "elements", source = "elements")
    @Mapping(target = "totalCost", expression = "java(calcTotal(calculation))")
    CalculationResponse toResponse(Calculation calculation);

    default BigDecimal calcTotal(Calculation calculation) {
        return calculation.getElements().stream()
                .flatMap(e -> e.getResultItems().stream())
                .map(i -> i.getTotalPrice() != null ? i.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}